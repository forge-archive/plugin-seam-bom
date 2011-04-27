/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.PackagingFacet;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.spec.javaee.CDIFacet;

/**
 * Installs the Seam BOM as a dependency to a project.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Alias("forge.seam3")
@RequiresFacet({ DependencyFacet.class, PackagingFacet.class, CDIFacet.class })
public class SeamFacet extends BaseFacet {


    @Inject
    private ShellPrompt shellPrompt;

    @Inject
    private ShellPrintWriter shellWriter;

    private void printPackTypeWarning(final Project project, final PackagingType type) {
        shellWriter.print(ShellColor.RED, "***WARNING*** ");
        shellWriter.println("Unsupported packaging type detected [" + type.getType());
    }

    @Override
    public boolean install() {
        if (!isInstalled()) {
            PackagingType type = project.getFacet(PackagingFacet.class).getPackagingType();
            if (!PackagingType.JAR.equals(type) && !PackagingType.WAR.equals(type)) {
                printPackTypeWarning(project, type);
            }
            DependencyFacet deps = project.getFacet(DependencyFacet.class);
    
            List<Dependency> versions = deps.resolveAvailableVersions("org.jboss.seam:seam-bom:[,]");
            Dependency version = shellPrompt.promptChoiceTyped("Install which version of Seam BOM?", versions);
            deps.setProperty("seam-bom.version", version.getVersion());
            DependencyBuilder dep = DependencyBuilder.create("org.jboss.seam:seam-bom:${seam-bom.version}");
            dep.setPackagingType("pom");
            dep.setScopeType(ScopeType.IMPORT);
            if (!deps.hasManagedDependency(dep)) {
                deps.addManagedDependency(dep);
            }
        }

        return true;
    }

    @Override
    public boolean isInstalled() {
        Dependency dep = DependencyBuilder.create("org.jboss.seam:seam-bom");
        PackagingType packagingType = project.getFacet(PackagingFacet.class).getPackagingType();
        return project.getFacet(DependencyFacet.class).hasManagedDependency(dep)
            && (PackagingType.JAR.equals(packagingType) || PackagingType.WAR.equals(packagingType));
    }

}
