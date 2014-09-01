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
package org.nuxeo.ecm.jsf2.migration.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.nuxeo.ecm.jsf2.migration.report.FileReport;
import org.nuxeo.ecm.jsf2.migration.service.RuleDescriptor;

/**
 * A generic parser only looking if an element is present in the file. If the
 * migration is activated, it replaces the element by the JSF2 compatible one.
 *
 * @since 5.9.6
 */
public class GenericParser implements
        RuleParser {

    private final String patternPrefix = "/[a-Z0-9]+:[a-Z0-9]+";

    private RuleDescriptor rule;

    private String xpath;

    private Map<String, String> listPrefixes;

    @Override
    public void init(
            RuleDescriptor rule,
            Map<String, String> listPrefixes) {
        xpath = rule.getXPath();
        this.listPrefixes = listPrefixes;
        this.rule = rule;
    }


    @Override
    public void parse(Document input, FileReport report) throws Exception {
        XPath xpathExpr = new Dom4jXPath(xpath);

        // Check if a namespace is needed
        List<String> prefixesInXpath = getPrefix(xpath);
        if (prefixesInXpath.size() > 0) {
            for (String prefixInXpath : prefixesInXpath) {
                // Check if the prefix is in the list of prefixes
                if (listPrefixes.containsKey(prefixInXpath)) {
                    SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                    nsContext.addNamespace(
                            prefixInXpath,
                            listPrefixes.get(prefixInXpath));
                    xpathExpr.setNamespaceContext(nsContext);
                } else {
                    // TODO throw an exception if the prefix is not declared
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<Element> elements = xpathExpr.selectNodes(input);
        if (elements.size() > 0) {
            List<String> params = new ArrayList<String>();
            params.add("" + elements.size());
            report.getListParams().put(rule.getName(), params);
            report.getListMigration().put(rule.getName(), elements.size());
        }
    }

    @Override
    public File migrate(File input) throws Exception {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    /**
     * If a prefix is defined in the XPath expression, it is returned.
     *
     * @param xpath XPath to check
     * @return The value of the prefix if present.
     */
    private List<String> getPrefix(
            String xpath) {
        List<String> listPrefixes = new ArrayList<String>();

        if (!StringUtils.isEmpty(xpath)) {
            Pattern pattern = Pattern.compile(patternPrefix);
            Matcher matcher = pattern.matcher(xpath);
            while (matcher.find()) {
                String prefix = matcher.group();
                // Get only the left part to get the prefix
                prefix = prefix.split(":")[0];
                // Remove the first character which is '/'
                prefix = prefix.substring(1);
                listPrefixes.add(prefix);
            }
        }

        return listPrefixes;
    }
}
