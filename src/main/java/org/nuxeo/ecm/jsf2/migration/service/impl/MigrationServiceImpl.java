/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     <a href="mailto:glefevre@nuxeo.com">Gildas</a>
 */
package org.nuxeo.ecm.jsf2.migration.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.nuxeo.ecm.jsf2.migration.analyzer.RuleParser;
import org.nuxeo.ecm.jsf2.migration.report.EnumTypeMigration;
import org.nuxeo.ecm.jsf2.migration.report.FileReport;
import org.nuxeo.ecm.jsf2.migration.service.PrefixDescriptor;
import org.nuxeo.ecm.jsf2.migration.service.RuleDescriptor;
import org.nuxeo.ecm.jsf2.migration.service.api.MigrationService;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Implementation of the services to help the migration to JSF 2.
 *
 * @since 5.9.6
 */
public class MigrationServiceImpl extends DefaultComponent implements MigrationService {

    private static final String PREFIX_EXTENSION = "prefixes";

    private static final String RULE_EXTENSION = "rules";

    private static final String FILE_EXTENSION = "xhtml";

    private static final String NOTHING_MESSAGE = "file.migration.nothing.message";

    private static final HashMap<String, String> listPrefixes = new HashMap<String, String>();

    private static final HashMap<String, RuleDescriptor> listRules = new HashMap<String, RuleDescriptor>();

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (StringUtils.equals(extensionPoint, PREFIX_EXTENSION)) {
            PrefixDescriptor prefix = (PrefixDescriptor) contribution;
            listPrefixes.put(prefix.getPrefix(), prefix.getNamespace());
        } else if (StringUtils.equals(extensionPoint, RULE_EXTENSION)) {
            RuleDescriptor rule = (RuleDescriptor) contribution;
            listRules.put(rule.getName(), rule);
        }
    }

    @Override
    public List<File> getAllXhtmlFiles(File root) {
        List<File> listFiles = new ArrayList<File>();

        // Parse all files in the directory
        for (File children : root.listFiles()) {
            if (children.isDirectory()) {
                listFiles.addAll(getAllXhtmlFiles(children));
            }
            if (children.isFile()) {
                String extension = FilenameUtils.getExtension(children.getName());
                if (FILE_EXTENSION.equals(extension)) {
                    listFiles.add(children);
                }
            }
        }

        return listFiles;
    }

    @Override
    public void analyzeProject(File report, List<File> listFiles,
            boolean doMigration) throws IOException {
        // If the file does not exist, it is created
        if (!report.exists()) {
            report.createNewFile();
        }

        PrintWriter printWriter = new PrintWriter(report);
        printWriter.append("##############################\n");
        printWriter.append("# Migration report for JSF 2 #\n");
        printWriter.append("##############################\n\n");

        List<FileReport> listReports = new ArrayList<FileReport>();
        for (File file : listFiles) {
            try {
                listReports.add(analyzeFile(file, doMigration));
            } catch(DocumentException ex) {
                System.out.println(String.format("Error while reading file %s.", file.getName()));
                System.out.println(ex.getMessage());
            } catch(JaxenException jex) {
                System.out.println(String.format("Error while parsing file %s.", file.getName()));
                System.out.println(jex.getMessage());
            }
        }

        // Generate the content report
        generateReport(listReports, printWriter);

        printWriter.close();
    }

    /**
     * Method to generate the final report.
     *
     * @param listResults List of result of analyze of each file.
     * @param report The text output stream of the report to complete.
     */
    private void generateReport(List<FileReport> listResults, PrintWriter report)
            throws IOException{
        // Load the file containing the messages to display in the report
        Properties reportProp = new Properties();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(
                "report.properties");
        reportProp.load(is);

        generateSummaryReport(listResults, report, reportProp);

        generateDetailedReport(listResults, report, reportProp);
    }

    /**
     * Generate the summary part of the report.
     *
     * @param listResults List of result of analyze of each file.
     * @param report The text output stream of the report to complete.
     * @param reportProp Properties file containing the message to display.
     * @throws IOException
     */
    private void generateSummaryReport(List<FileReport> listResults, PrintWriter report,
            Properties reportProp) throws IOException {

        report.append("Summary\n");
        report.append("#######\n");
        report.append("Number of files analyzed : " + listResults.size() + "\n");

        for (EnumTypeMigration type : EnumTypeMigration.values()) {
            int occurence = 0;
            for (FileReport fileReport : listResults) {
                if (fileReport.getListMigration().containsKey(type)) {
                    occurence += fileReport.getListMigration().get(type);
                }
            }

            // If the type of migration is present, it's added to the report
            if (occurence > 0) {
                report.append(" * ");
                report.append(MessageFormat.format(reportProp.getProperty(type.getKeyMessage()), occurence));
                report.append('\n');
            }
        }

        report.append("\n");
    }

    /**
     * Generate the summary part of the report.
     *
     * @param listResults List of result of analyze of each file.
     * @param report The text output stream of the report to complete.
     * @param reportProp Properties file containing the message to display.
     * @throws IOException
     */
    private void generateDetailedReport(List<FileReport> listResults, PrintWriter report,
            Properties reportProp) throws IOException {
        report.append("Details\n");
        report.append("#######");
        for (FileReport result : listResults) {
            report.append('\n');
            // Create a section for the file
            report.append(result.getAttachedFile().getName());
            report.append("\n-----------------------\n");

            // If nothing was reported, display a generic message
            if (result.getListMigration().size() == 0) {
                report.append(reportProp.getProperty(NOTHING_MESSAGE));
                report.append('\n');
            }

            // Get the actions to do for the migration
            for (String type : result.getListMigration().keySet()) {
                List<String> listParams = result.getListParams().get(type);
                String messageReport = MessageFormat.format(
                        reportProp.getProperty(type),
                        listParams.toArray());
                report.append(messageReport);
                report.append('\n');
            }
        }
    }

    @Override
    public FileReport analyzeFile(File file, boolean doMigration)
            throws JaxenException, DocumentException {
        FileReport fileReport = new FileReport(file);

        SAXReader reader = new SAXReader();
        Document xhtmlDoc;

        try {
            xhtmlDoc = reader.read(file);
            for (RuleDescriptor ruleDesc : listRules.values()) {
                RuleParser parser = ruleDesc.getParserInstance(listPrefixes);
                parser.parse(xhtmlDoc, fileReport);
            }
            /*for (EnumTypeMigration type : EnumTypeMigration.getTypesMigration()) {
                // Do the specific processing for some migration rules
                switch (type) {
                case NAMESPACE_RULE_2:
                    analyzeMigrationChangeNamespace(type, xhtmlDoc, fileReport);
                    break;
                default:
                    //analyzeMigrationRuleWithXPath(type, xhtmlDoc, fileReport);
                    break;
                }

            }*/
        } catch(DocumentException docEx) {
            // A parsing exception occured, the error is loaded in the FileReport.
            List<String> params = new ArrayList<String>();
            params.add(docEx.getMessage());
            // TODO fileReport.getListParams().put(EnumTypeMigration.NAMESPACE_RULE_1, params);
            // TODO fileReport.getListMigration().put(EnumTypeMigration.NAMESPACE_RULE_1, 1);
        } catch(Exception ex) {
            // TODO
        }

        return fileReport;
    }

    /*
     * Method analyzing a file when a rule with a change of namespace has been defined.
     * @param type Type of migration rule.
     * @param xhtmlDoc The document to analyze.
     * @param fileReport The FileReport object to complete after the analysis.
     *
    private void analyzeMigrationChangeNamespace(EnumTypeMigration type, Document xhtmlDoc, FileReport fileReport) {
        Element rootElement = xhtmlDoc.getRootElement();
        Namespace namespace = rootElement.getNamespaceForPrefix(type.getPrefix());
        if (namespace != null && StringUtils.equals(namespace.getURI(), type.getNamespace())){
            List<String> params = new ArrayList<String>();
            params.add("1");
            fileReport.getListParams().put(type, params);
            fileReport.getListMigration().put(type, 1);
        }
    }*/
}
