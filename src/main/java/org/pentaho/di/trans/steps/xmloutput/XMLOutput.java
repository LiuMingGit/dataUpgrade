package org.pentaho.di.trans.steps.xmloutput;

/**
 * 日期：2019年03月01日
 * 作者：刘铭
 * 邮箱：liuming@bsoft.com.cn
 */

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * 重写kettle xml输出类，已应对自身需求
 */
public class XMLOutput extends BaseStep implements StepInterface {
    private XMLOutputMeta meta;
    private XMLOutputData data;

    public XMLOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        this.meta = (XMLOutputMeta)smi;
        this.data = (XMLOutputData)sdi;
        boolean result = true;
        Object[] r = this.getRow();
        if (this.first && this.meta.isDoNotOpenNewFileInit()) {
            if (r == null) {
                this.setOutputDone();
                return false;
            }

            if (!this.openNewFile()) {
                this.logError("Couldn't open file " + this.meta.getFileName());
                this.setErrors(1L);
                return false;
            }

            this.data.OpenedNewFile = true;
        }

        if (r != null && this.getLinesOutput() > 0L && this.meta.getSplitEvery() > 0 && this.getLinesOutput() % (long)this.meta.getSplitEvery() == 0L) {
            this.closeFile();
            if (r != null && !this.openNewFile()) {
                this.logError("Unable to open new file (split #" + this.data.splitnr + "...");
                this.setErrors(1L);
                return false;
            }
        }

        if (r == null) {
            this.setOutputDone();
            return false;
        } else {
            this.writeRowToFile(this.getInputRowMeta(), r);
            this.data.outputRowMeta = this.getInputRowMeta().clone();
            this.meta.getFields(this.data.outputRowMeta, this.getStepname(), (RowMetaInterface[])null, (StepMeta)null, this);
            this.putRow(this.data.outputRowMeta, r);
            if (this.checkFeedback(this.getLinesOutput())) {
                this.logBasic("linenr " + this.getLinesOutput());
            }

            return result;
        }
    }

    private void writeRowToFile(RowMetaInterface rowMeta, Object[] r) throws KettleException {
        try {
            int i;
            ValueMetaInterface valueMeta;
            if (this.first) {
                this.data.formatRowMeta = rowMeta.clone();
                this.first = false;
                this.data.fieldnrs = new int[this.meta.getOutputFields().length];

                for(i = 0; i < this.meta.getOutputFields().length; ++i) {
                    this.data.fieldnrs[i] = this.data.formatRowMeta.indexOfValue(this.meta.getOutputFields()[i].getFieldName());
                    if (this.data.fieldnrs[i] < 0) {
                        throw new KettleException("Field [" + this.meta.getOutputFields()[i].getFieldName() + "] couldn't be found in the input stream!");
                    }

                    valueMeta = this.data.formatRowMeta.getValueMeta(this.data.fieldnrs[i]);
                    XMLField field = this.meta.getOutputFields()[i];
                    valueMeta.setConversionMask(field.getFormat());
                    valueMeta.setLength(field.getLength(), field.getPrecision());
                    valueMeta.setDecimalSymbol(field.getDecimalSymbol());
                    valueMeta.setGroupingSymbol(field.getGroupingSymbol());
                    valueMeta.setCurrencySymbol(field.getCurrencySymbol());
                }
            }

            if (this.meta.getOutputFields() != null && this.meta.getOutputFields().length != 0) {
                this.data.writer.write((" <" + this.meta.getRepeatElement() + ">").toCharArray());

                for(i = 0; i < this.meta.getOutputFields().length; ++i) {
                    XMLField outputField = this.meta.getOutputFields()[i];
                    if (i > 0) {
                        this.data.writer.write(32);
                    }

                    valueMeta = this.data.formatRowMeta.getValueMeta(this.data.fieldnrs[i]);
                    Object valueData = r[this.data.fieldnrs[i]];
                    String elementName = outputField.getElementName();
                    if (Const.isEmpty(elementName)) {
                        elementName = outputField.getFieldName();
                    }

                    if (!valueMeta.isNull(valueData) || !this.meta.isOmitNullValues()) {
                        this.writeField(valueMeta, valueData, elementName);
                    }
                }
            } else {
                this.data.writer.write((" <" + this.meta.getRepeatElement() + ">").toCharArray());

                for(i = 0; i < this.data.formatRowMeta.size(); ++i) {
                    if (i > 0) {
                        this.data.writer.write(32);
                    }

                    valueMeta = this.data.formatRowMeta.getValueMeta(i);
                    Object valueData = r[i];
                    this.writeField(valueMeta, valueData, valueMeta.getName());
                }
            }

            this.data.writer.write((" </" + this.meta.getRepeatElement() + ">").toCharArray());
            this.data.writer.write(Const.CR.toCharArray());
        } catch (Exception var8) {
            throw new KettleException("Error writing XML row :" + var8.toString() + Const.CR + "Row: " + this.getInputRowMeta().getString(r), var8);
        }

        this.incrementLinesOutput();
    }

    private void writeField(ValueMetaInterface valueMeta, Object valueData, String element) throws KettleStepException {
        try {
            String str = XMLHandler.addTagValue(element, valueMeta.getString(valueData), false, new String[0]);
            if (str != null) {
                this.data.writer.write(str.toCharArray());
            }

        } catch (Exception var5) {
            throw new KettleStepException("Error writing line :", var5);
        }
    }

    public String buildFilename(boolean ziparchive) {
        return this.meta.buildFilename(this, this.getCopy(), this.data.splitnr, ziparchive);
    }

    public boolean openNewFile() {
        boolean retval = false;
        this.data.writer = null;

        try {
            if (this.meta.isServletOutput()) {
                this.data.writer = this.getTrans().getServletPrintWriter();
                if (this.meta.getEncoding() != null && this.meta.getEncoding().length() > 0) {
                    this.data.writer.write(XMLHandler.getXMLHeader(this.meta.getEncoding()).toCharArray());
                } else {
                    this.data.writer.write(XMLHandler.getXMLHeader("UTF-8").toCharArray());
                }
            } else {
                FileObject file = KettleVFS.getFileObject(this.buildFilename(true), this.getTransMeta());
                if (this.meta.isAddToResultFiles()) {
                    ResultFile resultFile = new ResultFile(0, file, this.getTransMeta().getName(), this.getStepname());
                    resultFile.setComment("This file was created with a xml output step");
                    this.addResultFile(resultFile);
                }

                OutputStream fos;
                Object outputStream;
                if (this.meta.isZipped()) {
                    fos = KettleVFS.getOutputStream(file, false);
                    this.data.zip = new ZipOutputStream(fos);
                    File entry = new File(this.buildFilename(false));
                    ZipEntry zipentry = new ZipEntry(entry.getName());
                    zipentry.setComment("Compressed by Kettle");
                    this.data.zip.putNextEntry(zipentry);
                    outputStream = this.data.zip;
                } else {
                    fos = KettleVFS.getOutputStream(file, true);
                    outputStream = fos;
                }
                long fileSize = file.getContent().getSize();
                if (this.meta.getEncoding() != null && this.meta.getEncoding().length() > 0) {
                    this.logBasic("Opening output stream in encoding: " + this.meta.getEncoding());
                    this.data.writer = new OutputStreamWriter((OutputStream)outputStream, this.meta.getEncoding());
                    if(fileSize == 0)
                        this.data.writer.write(XMLHandler.getXMLHeader(this.meta.getEncoding()).toCharArray());
                } else {
                    this.logBasic("Opening output stream in default encoding : UTF-8");
                    this.data.writer = new OutputStreamWriter((OutputStream)outputStream);
                    if(fileSize == 0)
                        this.data.writer.write(XMLHandler.getXMLHeader("UTF-8").toCharArray());
                }
            }

            StringBuffer nameSpace = new StringBuffer();
            if (this.meta.getNameSpace() != null && !"".equals(this.meta.getNameSpace())) {
                nameSpace.append(" xmlns=\"");
                nameSpace.append(this.meta.getNameSpace());
                nameSpace.append("\"");
            }

            this.data.writer.write(("<" + this.meta.getMainElement() + nameSpace.toString() + ">" + Const.CR).toCharArray());
            retval = true;
        } catch (Exception var7) {
            this.logError("Error opening new file : " + var7.toString());
        }

        ++this.data.splitnr;
        return retval;
    }

    private boolean closeFile() {
        boolean retval = false;
        if (this.data.OpenedNewFile) {
            try {
                this.data.writer.write(("</" + this.meta.getMainElement() + ">" + Const.CR).toCharArray());
                this.data.writer.close();
                if (this.meta.isZipped()) {
                    this.data.zip.closeEntry();
                    this.data.zip.finish();
                    this.data.zip.close();
                }

                retval = true;
            } catch (Exception var3) {
            }
        }

        return retval;
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        this.meta = (XMLOutputMeta)smi;
        this.data = (XMLOutputData)sdi;
        if (super.init(smi, sdi)) {
            this.data.splitnr = 0;
            if (this.meta.isDoNotOpenNewFileInit()) {
                return true;
            }

            if (this.openNewFile()) {
                this.data.OpenedNewFile = true;
                return true;
            }

            this.logError("Couldn't open file " + this.meta.getFileName());
            this.setErrors(1L);
            this.stopAll();
        }

        return false;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        this.meta = (XMLOutputMeta)smi;
        this.data = (XMLOutputData)sdi;
        this.closeFile();
        super.dispose(smi, sdi);
    }
}
