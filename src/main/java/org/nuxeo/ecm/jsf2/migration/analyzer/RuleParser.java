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
 * Interface for all rule parser used to analyze a file for the migration to
 * jsf2.
 *
 * @since 5.9.6
 */
public interface RuleParser {

    /**
     * Init the RuleParser with the data from the RuleDeclaration and the list
     * of prefixes available.
     *
     * @param rule
     * @param listPrefixes
     */
    public void init(
            RuleDescriptor rule,
            Map<String, String> listPrefixes);

    /**
     * Apply the rule to the file and generate a FileReport object containing
     * the result;
     *
     * @param input The parsed document.
     * @param report The FileReport file.
     */
    public void parse(Document input, FileReport report) throws Exception;

    /**
     * Do the JSF 2 migration on the file.
     *
     * @return Return the updated file with the migrations.
     */
    public File migrate(File input) throws Exception;
}
