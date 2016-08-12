package org.orienteer.bpm.camunda.handler;

import com.orientechnologies.orient.core.metadata.schema.OType;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.orienteer.bpm.camunda.OPersistenceSession;
import org.orienteer.core.util.OSchemaHelper;

import java.util.List;
import java.util.Map;

/**
 * Created by KMukhov on 12.08.2016.
 */
public class DecisionDefinitionEntityHandler extends AbstractEntityHandler<DecisionDefinitionEntity> {

    public static final String OCLASS_NAME = "BPMDecisionDefinition";

    public DecisionDefinitionEntityHandler() {
        super(OCLASS_NAME);
    }

    @Override
    public void applySchema(OSchemaHelper helper) {
        super.applySchema(helper);

        helper.oProperty("category", OType.STRING, 10)
                .oProperty("name", OType.STRING, 20)
                .oProperty("key", OType.STRING, 30)
                .oProperty("version", OType.INTEGER, 40)
                .oProperty("deploymentId", OType.STRING, 50)
                .oProperty("resourceName", OType.STRING, 60)
                .oProperty("diagramResourceName", OType.STRING, 70)
                .oProperty("decisionRequirementsDefinitionId", OType.STRING, 80)
                .oProperty("tenantId", OType.STRING, 90);
    }

    @Statement
    public List<DecisionDefinitionEntity> selectDecisionDefinitionByDeploymentId(OPersistenceSession session, ListQueryParameterObject parameter) {
        return queryList(session, "select from " + getSchemaClass() + " where deploymentId=?", parameter.getParameter());
    }

    @Statement
    public List<DecisionDefinitionEntity> selectDecisionDefinitionByDeploymentAndKey(OPersistenceSession session, ListQueryParameterObject parameter) {
        Map<String, String> params = (Map<String, String>) parameter.getParameter();
        return queryList(session, "select from " + getSchemaClass() + " where deploymentId=? and key=?",
                params.get("deploymentId"), params.get("decisionDefinitionKey"));
    }

    @Statement
    public List<DecisionDefinitionEntity> selectLatestDecisionDefinitionByKey(OPersistenceSession session, ListQueryParameterObject parameter) {
        return queryList(session, "select from " + getSchemaClass() + " d1 inner join (select key, tenantId, max(version) as MAX_VERSION from " +
                getSchemaClass() + " where key = ? group by tenantId, key) d2 on d1.key = d2.key where d1.version = d2.MAX_VERSION " +
                "and (d1.tenantId = d2.tenantId or (d1.tenantId is null and d2.tenantId is null))", parameter.getParameter());
    }

    @Statement
    public List<DecisionDefinitionEntity> selectLatestDecisionDefinitionByKeyWithoutTenantId(OPersistenceSession session, ListQueryParameterObject parameter) {
        Map<String, String> params = (Map<String, String>) parameter.getParameter();
        String key = params.get("key");
        String tenantId = params.get("tenantId");

        return queryList(session, "select from " + getSchemaClass() + " where key = ? and tenantId = ? and version = (" +
                "select max(version) from " + getSchemaClass() + " where key = ? and tenantId = ?)",
                key, tenantId, key, tenantId);
    }
}
