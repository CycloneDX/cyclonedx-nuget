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

import com.microsoft.schemas.packaging.x2013.x05.nuspec.PackageDocument;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.xmlbeans.XmlOptions;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NugetClient {

    private static final String BASE_URL = "https://api.nuget.org/v3-flatcontainer/";
    private final HttpClient httpClient;
    private static final XmlOptions XML_OPTIONS = new XmlOptions();
    static {
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("http://schemas.microsoft.com/packaging/2012/06/nuspec.xsd", "http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd");
        namespaces.put("http://schemas.microsoft.com/packaging/2013/01/nuspec.xsd", "http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd");
        namespaces.put("http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd", "http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd");
        namespaces.put("http://schemas.microsoft.com/packaging/2010/07/nuspec.xsd", "http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd");
        XML_OPTIONS.setLoadSubstituteNamespaces(namespaces);
    }

    public NugetClient() {
        this.httpClient = HttpClientBuilder.create().build();
    }

    public NugetClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public PackageDocument retrievePackage(String id, String version) throws Exception {
        URL url = new URL(BASE_URL + id + "/" + version + "/" + id + ".nuspec");
        HttpUriRequest request = new HttpGet(url.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() == 200) {
            return PackageDocument.Factory.parse(response.getEntity().getContent(), XML_OPTIONS);
        }
        return null;
    }
}
