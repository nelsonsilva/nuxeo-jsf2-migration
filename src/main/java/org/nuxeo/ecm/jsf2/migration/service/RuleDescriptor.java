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
package org.nuxeo.ecm.jsf2.migration.service;

import java.io.Serializable;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.jsf2.migration.analyzer.RuleParser;

/**
 * Descriptor of a Rule object.
 *
 * @since 5.9.6
 */
@XObject("rule")
public class RuleDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    protected RuleParser instance;

    @XNode("@name")
    protected String name;

    @XNode("xpath")
    protected String xpath;

    @XNode("automaticMigration")
    protected String automaticMigration;

    @XNode("severity")
    protected String severity;

    @XNode("keyMessage")
    protected String keyMessage;

    @XNode("analyzer")
    protected Class analyzer;

    public String getName() {
        return name;
    }

    public String getXPath() {
        return xpath;
    }

    public boolean isAutomaticMigration() {
        return Boolean.parseBoolean(automaticMigration);
    }

    public String getMessageForDetailedReport() {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    public String getMessageForSummarizedReport() {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

    /**
     * Init the parser.
     * @param listPrefixes The list of prefixes defined in the contribution.
     * @throws Exception
     */
    public void initParser(Map<String, String> listPrefixes) throws Exception {
        if (instance == null) {
            instance = (RuleParser) analyzer.newInstance();
        }
        instance.init(this, listPrefixes);
    }

    /**
     * Get the instance of the parser defined in the contribution.
     *
     * @param listPrefixes The list of prefixes defined in the contribution.
     */
    public RuleParser getParserInstance(Map<String, String> listPrefixes) {
        try {
            initParser(listPrefixes);
        } catch (Exception ex) {
            // TODO
        }

        return instance;
    }
}
