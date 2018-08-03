[![Build Status](https://travis-ci.org/CycloneDX/cyclonedx-nuget.svg?branch=master)](https://travis-ci.org/CycloneDX/cyclonedx-nuget)
[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)][License]
[![Website](https://img.shields.io/badge/https://-cyclonedx.org-blue.svg)](https://cyclonedx.org/)
[![Twitter](https://img.shields.io/twitter/url/http/shields.io.svg?style=social&label=Follow)](https://twitter.com/CycloneDX_Spec)


CycloneDX for NuGet
=========

The CycloneDX for Nuget creates an aggregate of all dependencies and transitive dependencies of a project 
and creates a valid CycloneDX bill-of-material document from the results. CycloneDX is a lightweight BoM 
specification that is easily created, human readable, and simple to parse. The resulting bom.xml can be used
with tools such as [OWASP Dependency-Track](https://dependencytrack.org/) for the continuous analysis of components.

Usage
-------------------

```bash
java -jar cyclonedx-nuget
```

```bash
java -jar cyclonedx-nuget -v3a --in /path/to/project.assets.jso --out /output/path
```


Copyright & License
-------------------

CycloneDX for NuGet is Copyright (c) Steve Springett. All Rights Reserved.

Permission to modify and redistribute is granted under the terms of the Apache 2.0 license. See the [LICENSE] file for the full license.

[License]: https://github.com/CycloneDX/cyclonedx-nuget/blob/master/LICENSE
