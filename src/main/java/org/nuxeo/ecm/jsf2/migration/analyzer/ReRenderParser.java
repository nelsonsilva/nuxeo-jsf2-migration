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
import java.util.Map;

import org.dom4j.Document;
import org.nuxeo.ecm.jsf2.migration.report.FileReport;
import org.nuxeo.ecm.jsf2.migration.service.RuleDescriptor;

/**
 * Parser for the migration of the 'reRender' parameter.
 *
 * @since 5.9.6
 */
public class ReRenderParser implements
        RuleParser {

    private Map<String, String> listPrefixes;

    private String xpath;

    @Override
    public void init(
            RuleDescriptor rule,
            Map<String, String> listPrefixes) {
        this.listPrefixes = listPrefixes;
        this.xpath = rule.getXPath();
    }

    @Override
    public void parse(Document input,
            FileReport report)
            throws Exception {
        // TODO Auto-generated method stub
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public File migrate(File input) {
        // TODO Auto-generated method stub
        // return null;
        throw new UnsupportedOperationException();
    }

}
