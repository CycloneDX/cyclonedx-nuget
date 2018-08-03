/*
 * This file is part of CycloneDX for NuGet.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.cyclonedx.nuget;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.cyclonedx.model.Component;
import org.cyclonedx.nuget.v3.AssetsParser;
import org.cyclonedx.util.BomUtils;
import org.w3c.dom.Document;
import java.io.File;
import java.io.PrintWriter;
import java.util.Set;

public class BomGenerator {

    public enum SourceType {
        V3_ASSETS_JSON
    }

    private final SourceType sourceType;
    private final File inFile;
    private final File outFile;
    private boolean logToConsole = false;

    public BomGenerator(SourceType sourceType, File inFile, File outFile) {
        this.sourceType = sourceType;
        this.inFile = inFile;
        this.outFile = outFile;
    }

    public BomGenerator(SourceType sourceType, String inFile, String outFile) {
        this.sourceType = sourceType;
        this.inFile = new File(inFile);
        this.outFile = new File(outFile);
    }

    public Document generate() throws Exception {
        // Placeholder for all parsed components
        Set<Component> components = null;

        // NuGet version specific parsers that will generate a Set of Components when parsed
        if (SourceType.V3_ASSETS_JSON == sourceType) {
            AssetsParser parser = new AssetsParser();
            components = parser.parse(inFile);
        }

        // Parsing was successful. Attempt to create the BoM
        if (components != null) {
           return BomUtils.createBom(components);
        }
        return null;
    }

    public static void main (String[] args) throws Exception {
        final CommandLineParser parser = new DefaultParser();
        final Options options = new Options();

        options.addOption( "v3a", "nuget3-assets", false, "Parses NuGet v3 assets.json" );

        options.addOption(Option.builder().longOpt("in").desc("The NuGet file to parse")
                .hasArg().argName("in").build()
        );
        options.addOption(Option.builder().longOpt("out").desc("The target directory where bom.xml will be written")
                .hasArg().argName("out").build()
        );

        try {
            SourceType type = null;
            final CommandLine line = parser.parse(options, args);
            if (line.hasOption("nuget3-assets")) {
                type = SourceType.V3_ASSETS_JSON;
            }
            if (type == null) {
                System.err.println("Invalid NuGet option specified. Unable to determine what type of NuGet file to process.");
                displayHelp(options);
                return;
            }
            if (!line.hasOption("in")) {
                System.err.println("A source input file was not specified.");
                displayHelp(options);
                return;
            }
            if (!line.hasOption("out")) {
                System.err.println("A output target directory was not specified.");
                displayHelp(options);
                return;
            }

            final File inFile = new File(line.getOptionValue("in"));
            final File outFile = new File(line.getOptionValue("out"));
            if (!inFile.exists()) {
                System.err.println("Input file does not exist");
                displayHelp(options);
                return;
            }
            if (!outFile.exists() || !outFile.isDirectory()) {
                System.err.println("Output directory does not exist or is not a directory");
                displayHelp(options);
                return;
            }

            final BomGenerator generator = new BomGenerator(type, inFile, outFile);
            Document bom = generator.generate();
            if (bom != null) {
                try (PrintWriter out = new PrintWriter(new File(outFile, "/bom.xml"))) {
                    out.println(BomUtils.toString(bom));
                }
            }
        } catch (ParseException e) {
            displayHelp(options);
        }
    }

    private static void displayHelp(Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("cyclonedx-nuget", options);
    }

}
