package org.orienteer.core.boot.loader.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.orienteer.core.boot.loader.util.artifact.OArtifact;
import org.orienteer.core.boot.loader.util.artifact.OModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Vitaliy Gonchar
 * Class for read {@link OModule} from metadata.xml
 */
class OMetadataReader {
    private static final Logger LOG = LoggerFactory.getLogger(OMetadataReader.class);

    private final Path pathToMetadata;

    @VisibleForTesting OMetadataReader(Path pathToMetadata) {
        this.pathToMetadata = pathToMetadata;
    }

    @VisibleForTesting List<OModule> readModulesForLoad() {
        List<OModule> modules = read();
        List<OModule> modulesForLoad = Lists.newArrayList();
        for (OModule module : modules) {
            if (module.isLoad()) modulesForLoad.add(module);
        }
        return modulesForLoad;
    }

    @VisibleForTesting List<OModule> readAllModules() {
        return read();
    }

    @SuppressWarnings("unchecked")
    private List<OModule> read() {
        Document document = readFromFile();
        Element rootElement = document.getRootElement();
        return (List<OModule>) getModulesInMetadataXml(rootElement.elements(MetadataTag.MODULE.get()));
    }

    private List<OModule> getModulesInMetadataXml(List<Element> elements) {
        List<OModule> modules = Lists.newArrayList();
        for (Element element : elements) {
            OModule module = getModule(element);
            modules.add(module);
        }
        return modules;
    }

    @SuppressWarnings("unchecked")
    private OModule getModule(Element mainElement) {
        OModule module = new OModule();
        List<Element> elements = mainElement.elements();
        for (Element element : elements) {
            MetadataTag tag = MetadataTag.getByName(element.getName());
            switch (tag) {
                case LOAD:
                    module.setLoad(Boolean.valueOf(element.getText()));
                    break;
                case TRUSTED:
                    module.setTrusted(Boolean.valueOf(element.getText()));
                    break;
                case DEPENDENCY:
                    module.setArtifact(getMavenDependency(element));
                    break;
            }
        }
        return module;
    }

//    private List<Artifact> getDependencies(Element dependenciesElement) {
//        List<Artifact> dependencies = Lists.newArrayList();
//        List<Element> elements = dependenciesElement.elements(MetadataUtil.DEPENDENCY);
//        for (Element element : elements) {
//            if (element.getName().equals(MetadataUtil.DEPENDENCY)) {
//                dependencies.add(getMavenDependency(element));
//            }
//        }
//        return dependencies;
//    }

    @SuppressWarnings("unchecked")
    private OArtifact getMavenDependency(Element mainElement) {
        OArtifact artifact;
        String groupId    = null;
        String artifactId = null;
        String version    = null;
        String jar        = null;
        String repository = "";
        String description = "";
        List<Element> elements = mainElement.elements();
        for (Element element : elements) {
            MetadataTag tag = MetadataTag.getByName(element.getName());
            switch (tag) {
                case GROUP_ID:
                    groupId = element.getText();
                    break;
                case ARTIFACT_ID:
                    artifactId = element.getText();
                    break;
                case VERSION:
                    version = element.getText();
                    break;
                case REPOSITORY:
                    repository = element.getText();
                    break;
                case DESCRIPTION:
                    description = element.getText();
                    break;
                case JAR:
                    jar = element.getText();
                    break;
            }
        }
        artifact = new OArtifact(groupId, artifactId, version, repository, description);
        return jar != null ? artifact.setFile(new File(jar)) : artifact;
    }

    private Document readFromFile() {
        SAXReader reader = new SAXReader();
        try {
            return reader.read(pathToMetadata.toFile());
        } catch (DocumentException ex) {
            LOG.error("Cannot read metadata.xml", ex);
        }
        return null;
    }
}
