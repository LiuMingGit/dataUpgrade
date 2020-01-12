//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.Location;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.Parser.ParseException;
import org.codehaus.janino.Scanner.ScanException;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.getvariable.GetVariable;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef.ClassType;
import org.w3c.dom.Node;

public class UserDefinedJavaClassMeta extends BaseStepMeta implements StepMetaInterface {
    private static Class<?> PKG = UserDefinedJavaClassMeta.class;
    private List<UserDefinedJavaClassMeta.FieldInfo> fields = new ArrayList();
    private List<UserDefinedJavaClassDef> definitions = new ArrayList();
    public Class<TransformClassBase> cookedTransformClass;
    public List<Exception> cookErrors = new ArrayList(0);
    private boolean clearingResultFields;
    private boolean changed = true;
    private List<StepDefinition> infoStepDefinitions = new ArrayList();
    private List<StepDefinition> targetStepDefinitions = new ArrayList();
    private List<UsageParameter> usageParameters = new ArrayList();
    private DatabaseMeta databaseMeta;
    private boolean useBatchUpdate;
    public UserDefinedJavaClassMeta() {
    }

    public DatabaseMeta getDatabaseMeta() {
        return this.databaseMeta;
    }

    public void setDatabaseMeta(DatabaseMeta database) {
        this.databaseMeta = database;
    }

    private Class<?> cookClass(UserDefinedJavaClassDef def) throws CompileException, ParseException, ScanException, IOException, RuntimeException, KettleStepException {
        ClassBodyEvaluator cbe = new ClassBodyEvaluator();
        cbe.setClassName(def.getClassName());
        StringReader sr;
        if (def.isTransformClass()) {
            cbe.setExtendedType(TransformClassBase.class);
            sr = new StringReader(def.getTransformedSource());
        } else {
            sr = new StringReader(def.getSource());
        }

        cbe.setDefaultImports(new String[]{"org.pentaho.di.trans.steps.userdefinedjavaclass.*", "org.pentaho.di.trans.step.*", "org.pentaho.di.core.row.*", "org.pentaho.di.core.*", "org.pentaho.di.core.exception.*"});
        cbe.cook(new Scanner((String)null, sr));
        return cbe.getClazz();
    }

    public void cookClasses() {
        this.cookErrors.clear();
        Iterator i$ = this.getDefinitions().iterator();

        while(i$.hasNext()) {
            UserDefinedJavaClassDef def = (UserDefinedJavaClassDef)i$.next();
            if (def.isActive()) {
                try {
                    Class<?> cookedClass = this.cookClass(def);
                    if (def.isTransformClass()) {
                        this.cookedTransformClass = (Class<TransformClassBase>) cookedClass;
                    }
                } catch (Exception var5) {
                    CompileException exception = new CompileException(var5.getMessage(), (Location)null);
                    exception.setStackTrace(new StackTraceElement[0]);
                    this.cookErrors.add(exception);
                }
            }
        }

        this.changed = false;
    }
    public TransformClassBase newChildInstance(UserDefinedJavaClass parent, UserDefinedJavaClassMeta meta, UserDefinedJavaClassData data) {
        if (!this.checkClassCookings(this.getLog())) {
            return null;
        } else {
            try {
                return (TransformClassBase)this.cookedTransformClass.getConstructor(UserDefinedJavaClass.class, UserDefinedJavaClassMeta.class, UserDefinedJavaClassData.class).newInstance(parent, meta, data);
            } catch (Exception var6) {
                if (this.log.isDebug()) {
                    this.log.logError("Full debugging stacktrace of UserDefinedJavaClass instanciation exception:", var6.getCause());
                }

                KettleException kettleException = new KettleException(var6.getMessage());
                kettleException.setStackTrace(new StackTraceElement[0]);
                this.cookErrors.add(kettleException);
                return null;
            }
        }
    }

    public List<UserDefinedJavaClassMeta.FieldInfo> getFieldInfo() {
        return Collections.unmodifiableList(this.fields);
    }

    public void replaceFields(List<UserDefinedJavaClassMeta.FieldInfo> fields) {
        this.fields = fields;
        this.changed = true;
    }

    public List<UserDefinedJavaClassDef> getDefinitions() {
        return Collections.unmodifiableList(this.definitions);
    }

    public void replaceDefinitions(List<UserDefinedJavaClassDef> definitions) {
        this.definitions.clear();
        this.definitions.addAll(definitions);
        this.changed = true;
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
        this.readData(stepnode,databases);
    }

    public Object clone() {
        try {
            UserDefinedJavaClassMeta retval = (UserDefinedJavaClassMeta)super.clone();
            ArrayList newUsageParameters;
            Iterator i$;
            if (this.fields != null) {
                newUsageParameters = new ArrayList(this.fields.size());
                i$ = this.fields.iterator();

                while(i$.hasNext()) {
                    UserDefinedJavaClassMeta.FieldInfo field = (UserDefinedJavaClassMeta.FieldInfo)i$.next();
                    newUsageParameters.add((UserDefinedJavaClassMeta.FieldInfo)field.clone());
                }

                retval.fields = newUsageParameters;
            }

            if (this.definitions != null) {
                newUsageParameters = new ArrayList();
                i$ = this.definitions.iterator();

                while(i$.hasNext()) {
                    UserDefinedJavaClassDef def = (UserDefinedJavaClassDef)i$.next();
                    newUsageParameters.add((UserDefinedJavaClassDef)def.clone());
                }

                retval.definitions = newUsageParameters;
            }

            retval.cookedTransformClass = null;
            retval.cookErrors = new ArrayList(0);
            StepDefinition step;
            if (this.infoStepDefinitions != null) {
                newUsageParameters = new ArrayList();
                i$ = this.infoStepDefinitions.iterator();

                while(i$.hasNext()) {
                    step = (StepDefinition)i$.next();
                    newUsageParameters.add((StepDefinition)step.clone());
                }

                retval.infoStepDefinitions = newUsageParameters;
            }

            if (this.targetStepDefinitions != null) {
                newUsageParameters = new ArrayList();
                i$ = this.targetStepDefinitions.iterator();

                while(i$.hasNext()) {
                    step = (StepDefinition)i$.next();
                    newUsageParameters.add((StepDefinition)step.clone());
                }

                retval.targetStepDefinitions = newUsageParameters;
            }

            if (this.usageParameters != null) {
                newUsageParameters = new ArrayList();
                i$ = this.usageParameters.iterator();

                while(i$.hasNext()) {
                    UsageParameter param = (UsageParameter)i$.next();
                    newUsageParameters.add((UsageParameter)param.clone());
                }

                retval.usageParameters = newUsageParameters;
            }

            return retval;
        } catch (CloneNotSupportedException var5) {
            return null;
        }
    }

    private void readData(Node stepnode,List<DatabaseMeta> databases) throws KettleXMLException {
        try {

            this.databaseMeta = DatabaseMeta.findDatabase(databases, "");
            Node definitionsNode = XMLHandler.getSubNode(stepnode, UserDefinedJavaClassMeta.ElementNames.definitions.name());
            int nrDefinitions = XMLHandler.countNodes(definitionsNode, UserDefinedJavaClassMeta.ElementNames.definition.name());

            for(int i = 0; i < nrDefinitions; ++i) {
                Node fnode = XMLHandler.getSubNodeByNr(definitionsNode, UserDefinedJavaClassMeta.ElementNames.definition.name(), i);
                this.definitions.add(new UserDefinedJavaClassDef(ClassType.valueOf(XMLHandler.getTagValue(fnode, UserDefinedJavaClassMeta.ElementNames.class_type.name())), XMLHandler.getTagValue(fnode, UserDefinedJavaClassMeta.ElementNames.class_name.name()), XMLHandler.getTagValue(fnode, UserDefinedJavaClassMeta.ElementNames.class_source.name())));
            }

            Node fieldsNode = XMLHandler.getSubNode(stepnode, UserDefinedJavaClassMeta.ElementNames.fields.name());
            int nrfields = XMLHandler.countNodes(fieldsNode, UserDefinedJavaClassMeta.ElementNames.field.name());

            for(int i = 0; i < nrfields; ++i) {
                Node fnode = XMLHandler.getSubNodeByNr(fieldsNode, UserDefinedJavaClassMeta.ElementNames.field.name(), i);
                this.fields.add(new UserDefinedJavaClassMeta.FieldInfo(XMLHandler.getTagValue(fnode, UserDefinedJavaClassMeta.ElementNames.field_name.name()), ValueMeta.getType(XMLHandler.getTagValue(fnode, UserDefinedJavaClassMeta.ElementNames.field_type.name())), Const.toInt(XMLHandler.getTagValue(fnode, UserDefinedJavaClassMeta.ElementNames.field_length.name()), -1), Const.toInt(XMLHandler.getTagValue(fnode, UserDefinedJavaClassMeta.ElementNames.field_precision.name()), -1)));
            }

            this.setClearingResultFields(!"N".equals(XMLHandler.getTagValue(stepnode, UserDefinedJavaClassMeta.ElementNames.clear_result_fields.name())));
            this.infoStepDefinitions.clear();
            Node infosNode = XMLHandler.getSubNode(stepnode, UserDefinedJavaClassMeta.ElementNames.info_steps.name());
            int nrInfos = XMLHandler.countNodes(infosNode, UserDefinedJavaClassMeta.ElementNames.info_step.name());

            for(int i = 0; i < nrInfos; ++i) {
                Node infoNode = XMLHandler.getSubNodeByNr(infosNode, UserDefinedJavaClassMeta.ElementNames.info_step.name(), i);
                StepDefinition stepDefinition = new StepDefinition();
                stepDefinition.tag = XMLHandler.getTagValue(infoNode, UserDefinedJavaClassMeta.ElementNames.step_tag.name());
                stepDefinition.stepName = XMLHandler.getTagValue(infoNode, UserDefinedJavaClassMeta.ElementNames.step_name.name());
                stepDefinition.description = XMLHandler.getTagValue(infoNode, UserDefinedJavaClassMeta.ElementNames.step_description.name());
                this.infoStepDefinitions.add(stepDefinition);
            }

            this.targetStepDefinitions.clear();
            Node targetsNode = XMLHandler.getSubNode(stepnode, UserDefinedJavaClassMeta.ElementNames.target_steps.name());
            int nrTargets = XMLHandler.countNodes(targetsNode, UserDefinedJavaClassMeta.ElementNames.target_step.name());

            for(int i = 0; i < nrTargets; ++i) {
                Node targetNode = XMLHandler.getSubNodeByNr(targetsNode, UserDefinedJavaClassMeta.ElementNames.target_step.name(), i);
                StepDefinition stepDefinition = new StepDefinition();
                stepDefinition.tag = XMLHandler.getTagValue(targetNode, UserDefinedJavaClassMeta.ElementNames.step_tag.name());
                stepDefinition.stepName = XMLHandler.getTagValue(targetNode, UserDefinedJavaClassMeta.ElementNames.step_name.name());
                stepDefinition.description = XMLHandler.getTagValue(targetNode, UserDefinedJavaClassMeta.ElementNames.step_description.name());
                this.targetStepDefinitions.add(stepDefinition);
            }

            this.usageParameters.clear();
            Node parametersNode = XMLHandler.getSubNode(stepnode, UserDefinedJavaClassMeta.ElementNames.usage_parameters.name());
            int nrParameters = XMLHandler.countNodes(parametersNode, UserDefinedJavaClassMeta.ElementNames.usage_parameter.name());

            for(int i = 0; i < nrParameters; ++i) {
                Node parameterNode = XMLHandler.getSubNodeByNr(parametersNode, UserDefinedJavaClassMeta.ElementNames.usage_parameter.name(), i);
                UsageParameter usageParameter = new UsageParameter();
                usageParameter.tag = XMLHandler.getTagValue(parameterNode, UserDefinedJavaClassMeta.ElementNames.parameter_tag.name());
                usageParameter.value = XMLHandler.getTagValue(parameterNode, UserDefinedJavaClassMeta.ElementNames.parameter_value.name());
                usageParameter.description = XMLHandler.getTagValue(parameterNode, UserDefinedJavaClassMeta.ElementNames.parameter_description.name());
                this.usageParameters.add(usageParameter);
            }

        } catch (Exception var15) {
            throw new KettleXMLException(BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.Exception.UnableToLoadStepInfoFromXML", new String[0]), var15);
        }
    }

    public void setDefault() {
    }

    private boolean checkClassCookings(LogChannelInterface logChannel) {
        boolean ok = this.cookedTransformClass != null && this.cookErrors.size() == 0;
        if (this.changed) {
            this.cookClasses();
            if (this.cookedTransformClass == null) {
                if (this.cookErrors.size() > 0) {
                    logChannel.logDebug(BaseMessages.getString(PKG, "UserDefinedJavaClass.Exception.CookingError", new Object[]{this.cookErrors.get(0)}));
                }

                ok = false;
            } else {
                ok = true;
            }
        }

        return ok;
    }

    public StepIOMetaInterface getStepIOMeta() {
        if (!this.checkClassCookings(this.getLog())) {
            return super.getStepIOMeta();
        } else {
            try {
                Method getStepIOMeta = this.cookedTransformClass.getMethod("getStepIOMeta", UserDefinedJavaClassMeta.class);
                if (getStepIOMeta != null) {
                    StepIOMetaInterface stepIoMeta = (StepIOMetaInterface)getStepIOMeta.invoke((Object)null, this);
                    return stepIoMeta == null ? super.getStepIOMeta() : stepIoMeta;
                } else {
                    return super.getStepIOMeta();
                }
            } catch (Exception var3) {
                var3.printStackTrace();
                return super.getStepIOMeta();
            }
        }
    }

    public void searchInfoAndTargetSteps(List<StepMeta> steps) {
        Iterator i$;
        StepDefinition stepDefinition;
        for(i$ = this.infoStepDefinitions.iterator(); i$.hasNext(); stepDefinition.stepMeta = StepMeta.findStep(steps, stepDefinition.stepName)) {
            stepDefinition = (StepDefinition)i$.next();
        }

        for(i$ = this.targetStepDefinitions.iterator(); i$.hasNext(); stepDefinition.stepMeta = StepMeta.findStep(steps, stepDefinition.stepName)) {
            stepDefinition = (StepDefinition)i$.next();
        }

    }

    public void getFields(RowMetaInterface row, String originStepname, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
        if (!this.checkClassCookings(this.getLog())) {
            if (this.cookErrors.size() > 0) {
                throw new KettleStepException("Error initializing UserDefinedJavaClass to get fields: ", (Throwable)this.cookErrors.get(0));
            }
        } else {
            try {
                Method getFieldsMethod = this.cookedTransformClass.getMethod("getFields", Boolean.TYPE, RowMetaInterface.class, String.class, RowMetaInterface[].class, StepMeta.class, VariableSpace.class, List.class);
                getFieldsMethod.invoke((Object)null, this.isClearingResultFields(), row, originStepname, info, nextStep, space, this.getFieldInfo());
            } catch (Exception var7) {
                throw new KettleStepException("Error executing UserDefinedJavaClass.getFields(): ", var7);
            }
        }
    }

    public String getXML() {
        StringBuilder retval = new StringBuilder(300);
        retval.append(String.format("\n    <%s>", UserDefinedJavaClassMeta.ElementNames.definitions.name()));
        Iterator i$ = this.definitions.iterator();

        while(i$.hasNext()) {
            UserDefinedJavaClassDef def = (UserDefinedJavaClassDef)i$.next();
            retval.append(String.format("\n        <%s>", UserDefinedJavaClassMeta.ElementNames.definition.name()));
            retval.append("\n        ").append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.class_type.name(), def.getClassType().name()));
            retval.append("\n        ").append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.class_name.name(), def.getClassName()));
            retval.append("\n        ").append(XMLHandler.openTag(UserDefinedJavaClassMeta.ElementNames.class_source.name()));
            retval.append(XMLHandler.buildCDATA(def.getSource())).append(XMLHandler.closeTag(UserDefinedJavaClassMeta.ElementNames.class_source.name()));
            retval.append(String.format("\n        </%s>", UserDefinedJavaClassMeta.ElementNames.definition.name()));
        }

        retval.append(String.format("\n    </%s>", UserDefinedJavaClassMeta.ElementNames.definitions.name()));
        retval.append(String.format("\n    <%s>", UserDefinedJavaClassMeta.ElementNames.fields.name()));
        i$ = this.fields.iterator();

        while(i$.hasNext()) {
            UserDefinedJavaClassMeta.FieldInfo fi = (UserDefinedJavaClassMeta.FieldInfo)i$.next();
            retval.append(String.format("\n        <%s>", UserDefinedJavaClassMeta.ElementNames.field.name()));
            retval.append("\n        ").append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.field_name.name(), fi.name));
            retval.append("\n        ").append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.field_type.name(), ValueMeta.getTypeDesc(fi.type)));
            retval.append("\n        ").append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.field_length.name(), fi.length));
            retval.append("\n        ").append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.field_precision.name(), fi.precision));
            retval.append(String.format("\n        </%s>", UserDefinedJavaClassMeta.ElementNames.field.name()));
        }

        retval.append(String.format("\n    </%s>", UserDefinedJavaClassMeta.ElementNames.fields.name()));
        retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.clear_result_fields.name(), this.clearingResultFields));
        retval.append(XMLHandler.openTag(UserDefinedJavaClassMeta.ElementNames.info_steps.name()));
        i$ = this.infoStepDefinitions.iterator();

        StepDefinition stepDefinition;
        while(i$.hasNext()) {
            stepDefinition = (StepDefinition)i$.next();
            retval.append(XMLHandler.openTag(UserDefinedJavaClassMeta.ElementNames.info_step.name()));
            retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.step_tag.name(), stepDefinition.tag));
            retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.step_name.name(), stepDefinition.stepMeta != null ? stepDefinition.stepMeta.getName() : null));
            retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.step_description.name(), stepDefinition.description));
            retval.append(XMLHandler.closeTag(UserDefinedJavaClassMeta.ElementNames.info_step.name()));
        }

        retval.append(XMLHandler.closeTag(UserDefinedJavaClassMeta.ElementNames.info_steps.name()));
        retval.append(XMLHandler.openTag(UserDefinedJavaClassMeta.ElementNames.target_steps.name()));
        i$ = this.targetStepDefinitions.iterator();

        while(i$.hasNext()) {
            stepDefinition = (StepDefinition)i$.next();
            retval.append(XMLHandler.openTag(UserDefinedJavaClassMeta.ElementNames.target_step.name()));
            retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.step_tag.name(), stepDefinition.tag));
            retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.step_name.name(), stepDefinition.stepMeta != null ? stepDefinition.stepMeta.getName() : null));
            retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.step_description.name(), stepDefinition.description));
            retval.append(XMLHandler.closeTag(UserDefinedJavaClassMeta.ElementNames.target_step.name()));
        }

        retval.append(XMLHandler.closeTag(UserDefinedJavaClassMeta.ElementNames.target_steps.name()));
        retval.append(XMLHandler.openTag(UserDefinedJavaClassMeta.ElementNames.usage_parameters.name()));
        i$ = this.usageParameters.iterator();

        while(i$.hasNext()) {
            UsageParameter usageParameter = (UsageParameter)i$.next();
            retval.append(XMLHandler.openTag(UserDefinedJavaClassMeta.ElementNames.usage_parameter.name()));
            retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.parameter_tag.name(), usageParameter.tag));
            retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.parameter_value.name(), usageParameter.value));
            retval.append(XMLHandler.addTagValue(UserDefinedJavaClassMeta.ElementNames.parameter_description.name(), usageParameter.description));
            retval.append(XMLHandler.closeTag(UserDefinedJavaClassMeta.ElementNames.usage_parameter.name()));
        }

        retval.append(XMLHandler.closeTag(UserDefinedJavaClassMeta.ElementNames.usage_parameters.name()));
        return retval.toString();
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
        try {
            int nrScripts = rep.countNrStepAttributes(id_step, UserDefinedJavaClassMeta.ElementNames.class_name.name());

            int nrfields;
            for(nrfields = 0; nrfields < nrScripts; ++nrfields) {
                this.definitions.add(new UserDefinedJavaClassDef(ClassType.valueOf(rep.getStepAttributeString(id_step, nrfields, UserDefinedJavaClassMeta.ElementNames.class_type.name())), rep.getStepAttributeString(id_step, nrfields, UserDefinedJavaClassMeta.ElementNames.class_name.name()), rep.getStepAttributeString(id_step, nrfields, UserDefinedJavaClassMeta.ElementNames.class_source.name())));
            }

            nrfields = rep.countNrStepAttributes(id_step, UserDefinedJavaClassMeta.ElementNames.field_name.name());

            int nrInfos;
            for(nrInfos = 0; nrInfos < nrfields; ++nrInfos) {
                this.fields.add(new UserDefinedJavaClassMeta.FieldInfo(rep.getStepAttributeString(id_step, nrInfos, UserDefinedJavaClassMeta.ElementNames.field_name.name()), ValueMeta.getType(rep.getStepAttributeString(id_step, nrInfos, UserDefinedJavaClassMeta.ElementNames.field_type.name())), (int)rep.getStepAttributeInteger(id_step, nrInfos, UserDefinedJavaClassMeta.ElementNames.field_length.name()), (int)rep.getStepAttributeInteger(id_step, nrInfos, UserDefinedJavaClassMeta.ElementNames.field_precision.name())));
            }

            this.clearingResultFields = rep.getStepAttributeBoolean(id_step, UserDefinedJavaClassMeta.ElementNames.clear_result_fields.name());
            nrInfos = rep.countNrStepAttributes(id_step, UserDefinedJavaClassMeta.ElementNames.info_.name() + UserDefinedJavaClassMeta.ElementNames.step_name.name());

            int nrTargets;
            for(nrTargets = 0; nrTargets < nrInfos; ++nrTargets) {
                StepDefinition stepDefinition = new StepDefinition();
                stepDefinition.tag = rep.getStepAttributeString(id_step, nrTargets, UserDefinedJavaClassMeta.ElementNames.info_.name() + UserDefinedJavaClassMeta.ElementNames.step_tag.name());
                stepDefinition.stepName = rep.getStepAttributeString(id_step, nrTargets, UserDefinedJavaClassMeta.ElementNames.info_.name() + UserDefinedJavaClassMeta.ElementNames.step_name.name());
                stepDefinition.description = rep.getStepAttributeString(id_step, nrTargets, UserDefinedJavaClassMeta.ElementNames.info_.name() + UserDefinedJavaClassMeta.ElementNames.step_description.name());
                this.infoStepDefinitions.add(stepDefinition);
            }

            nrTargets = rep.countNrStepAttributes(id_step, UserDefinedJavaClassMeta.ElementNames.target_.name() + UserDefinedJavaClassMeta.ElementNames.step_name.name());

            int nrParameters;
            for(nrParameters = 0; nrParameters < nrTargets; ++nrParameters) {
                StepDefinition stepDefinition = new StepDefinition();
                stepDefinition.tag = rep.getStepAttributeString(id_step, nrParameters, UserDefinedJavaClassMeta.ElementNames.target_.name() + UserDefinedJavaClassMeta.ElementNames.step_tag.name());
                stepDefinition.stepName = rep.getStepAttributeString(id_step, nrParameters, UserDefinedJavaClassMeta.ElementNames.target_.name() + UserDefinedJavaClassMeta.ElementNames.step_name.name());
                stepDefinition.description = rep.getStepAttributeString(id_step, nrParameters, UserDefinedJavaClassMeta.ElementNames.target_.name() + UserDefinedJavaClassMeta.ElementNames.step_description.name());
                this.targetStepDefinitions.add(stepDefinition);
            }

            nrParameters = rep.countNrStepAttributes(id_step, UserDefinedJavaClassMeta.ElementNames.parameter_tag.name());

            for(int i = 0; i < nrParameters; ++i) {
                UsageParameter usageParameter = new UsageParameter();
                usageParameter.tag = rep.getStepAttributeString(id_step, i, UserDefinedJavaClassMeta.ElementNames.parameter_tag.name());
                usageParameter.value = rep.getStepAttributeString(id_step, i, UserDefinedJavaClassMeta.ElementNames.parameter_value.name());
                usageParameter.description = rep.getStepAttributeString(id_step, i, UserDefinedJavaClassMeta.ElementNames.parameter_description.name());
                this.usageParameters.add(usageParameter);
            }

        } catch (Exception var12) {
            throw new KettleException(BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.Exception.UnexpectedErrorInReadingStepInfo", new String[0]), var12);
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
        try {
            int i;
            for(i = 0; i < this.definitions.size(); ++i) {
                UserDefinedJavaClassDef def = (UserDefinedJavaClassDef)this.definitions.get(i);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.class_name.name(), def.getClassName());
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.class_source.name(), def.getSource());
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.class_type.name(), def.getClassType().name());
            }

            for(i = 0; i < this.fields.size(); ++i) {
                UserDefinedJavaClassMeta.FieldInfo fi = (UserDefinedJavaClassMeta.FieldInfo)this.fields.get(i);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.field_name.name(), fi.name);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.field_type.name(), ValueMeta.getTypeDesc(fi.type));
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.field_length.name(), (long)fi.length);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.field_precision.name(), (long)fi.precision);
            }

            rep.saveStepAttribute(id_transformation, id_step, UserDefinedJavaClassMeta.ElementNames.clear_result_fields.name(), this.clearingResultFields);

            StepDefinition stepDefinition;
            for(i = 0; i < this.infoStepDefinitions.size(); ++i) {
                stepDefinition = (StepDefinition)this.infoStepDefinitions.get(i);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.info_.name() + UserDefinedJavaClassMeta.ElementNames.step_tag.name(), stepDefinition.tag);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.info_.name() + UserDefinedJavaClassMeta.ElementNames.step_name.name(), stepDefinition.stepMeta != null ? stepDefinition.stepMeta.getName() : null);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.info_.name() + UserDefinedJavaClassMeta.ElementNames.step_description.name(), stepDefinition.description);
            }

            for(i = 0; i < this.targetStepDefinitions.size(); ++i) {
                stepDefinition = (StepDefinition)this.targetStepDefinitions.get(i);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.target_.name() + UserDefinedJavaClassMeta.ElementNames.step_tag.name(), stepDefinition.tag);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.target_.name() + UserDefinedJavaClassMeta.ElementNames.step_name.name(), stepDefinition.stepMeta != null ? stepDefinition.stepMeta.getName() : null);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.target_.name() + UserDefinedJavaClassMeta.ElementNames.step_description.name(), stepDefinition.description);
            }

            for(i = 0; i < this.usageParameters.size(); ++i) {
                UsageParameter usageParameter = (UsageParameter)this.usageParameters.get(i);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.parameter_tag.name(), usageParameter.tag);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.parameter_value.name(), usageParameter.value);
                rep.saveStepAttribute(id_transformation, id_step, i, UserDefinedJavaClassMeta.ElementNames.parameter_description.name(), usageParameter.description);
            }

        } catch (Exception var6) {
            throw new KettleException(BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.Exception.UnableToSaveStepInfo", new String[0]) + id_step, var6);
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
        CheckResult cr;
        if (input.length > 0) {
            cr = new CheckResult(1, BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.CheckResult.ConnectedStepOK2", new String[0]), stepinfo);
            remarks.add(cr);
        } else {
            cr = new CheckResult(4, BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.CheckResult.NoInputReceived", new String[0]), stepinfo);
            remarks.add(cr);
        }

    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans) {
        UserDefinedJavaClass userDefinedJavaClass = new UserDefinedJavaClass(stepMeta, stepDataInterface, cnr, transMeta, trans);
        return trans.hasHaltedSteps() ? null : userDefinedJavaClass;
    }

    public StepDataInterface getStepData() {
        return new UserDefinedJavaClassData();
    }

    public boolean supportsErrorHandling() {
        return true;
    }

    public boolean isClearingResultFields() {
        return this.clearingResultFields;
    }

    public void setClearingResultFields(boolean clearingResultFields) {
        this.clearingResultFields = clearingResultFields;
    }

    public List<StepDefinition> getInfoStepDefinitions() {
        return this.infoStepDefinitions;
    }

    public void setInfoStepDefinitions(List<StepDefinition> infoStepDefinitions) {
        this.infoStepDefinitions = infoStepDefinitions;
    }

    public List<StepDefinition> getTargetStepDefinitions() {
        return this.targetStepDefinitions;
    }

    public void setTargetStepDefinitions(List<StepDefinition> targetStepDefinitions) {
        this.targetStepDefinitions = targetStepDefinitions;
    }

    public boolean excludeFromRowLayoutVerification() {
        return true;
    }

    public boolean useBatchUpdate() {
        return this.useBatchUpdate;
    }

    public void setUseBatchUpdate(boolean useBatchUpdate) {
        this.useBatchUpdate = useBatchUpdate;
    }

    public List<UsageParameter> getUsageParameters() {
        return this.usageParameters;
    }

    public void setUsageParameters(List<UsageParameter> usageParameters) {
        this.usageParameters = usageParameters;
    }

    public static class FieldInfo implements Cloneable {
        public final String name;
        public final int type;
        public final int length;
        public final int precision;

        public FieldInfo(String name, int type, int length, int precision) {
            this.name = name;
            this.type = type;
            this.length = length;
            this.precision = precision;
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public static enum ElementNames {
        class_type,
        class_name,
        class_source,
        definitions,
        definition,
        fields,
        field,
        field_name,
        field_type,
        field_length,
        field_precision,
        clear_result_fields,
        info_steps,
        info_step,
        info_,
        target_steps,
        target_step,
        target_,
        step_tag,
        step_name,
        step_description,
        usage_parameters,
        usage_parameter,
        parameter_tag,
        parameter_value,
        parameter_description;

        private ElementNames() {
        }
    }
}
