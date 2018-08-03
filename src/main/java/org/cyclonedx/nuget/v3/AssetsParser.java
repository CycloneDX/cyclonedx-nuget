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
package org.cyclonedx.nuget.v3;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.microsoft.schemas.packaging.x2013.x05.nuspec.PackageDocument;
import org.apache.commons.io.FileUtils;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Hash;
import org.cyclonedx.nuget.NugetClient;
import org.json.JSONObject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class AssetsParser {

    public Set<Component> parse(File file) throws Exception {
        final NugetClient client = new NugetClient();
        final JSONObject root = new JSONObject(
                FileUtils.readFileToString(file, StandardCharsets.UTF_8)
        );
        final Set<Component> components = new HashSet<>();
        final JSONObject libraries = root.optJSONObject("libraries");
        for (String key : libraries.keySet()) {
            final JSONObject library = libraries.getJSONObject(key);
            final String path = library.optString("path");
            final int seperator = path.indexOf("/");
            final String id = path.substring(0, seperator);
            final String version = path.substring(seperator + 1, path.length());

            final PackageDocument packageDocument = client.retrievePackage(id, version);
            final PackageDocument.Package pkg = packageDocument.getPackage();
            final PackageDocument.Package.Metadata metadata = pkg.getMetadata();

            final Component component = new Component();
            component.setType("library"); // TODO - Can this be pre-determined from the nuspec data?
            component.setName(metadata.getId());
            component.setVersion(metadata.getVersion());
            component.setPublisher(metadata.getAuthors());
            component.setCopyright(metadata.getCopyright());

            if (metadata.getSummary() != null) {
                component.setDescription(metadata.getSummary());
            } else if (metadata.getDescription() != null) {
                component.setDescription(metadata.getDescription());
            } else {
                component.setDescription(metadata.getTitle());
            }


            //TODO: nuspec authors thought it would be a good idea to publish the URL to the license
            //rather than the SPDX identifier of the license itself. Genius! NOT! nuspec will need to
            //change the spec if they wish to provide accurate license information in boms.

            final String sha512 = library.optString("sha512");
            if (sha512 != null) {
                component.addHash(new Hash(Hash.Algorithm.SHA_512, sha512));
            }
            try {
                final PackageURL purl = new PackageURL(
                        PackageURL.StandardTypes.NUGET, null, component.getName(), component.getVersion(), null, null
                );
                component.setPurl(purl);
            } catch (MalformedPackageURLException e) {
                // todo : log this
            }
            components.add(component);
        }
        return components;
    }
}
