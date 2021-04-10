/*
* Copyright 2020 Expedia, Inc.
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
*/

package com.vrbo.jarviz.service;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.vrbo.jarviz.config.CouplingFilterConfig;
import com.vrbo.jarviz.model.Collector;
import com.vrbo.jarviz.model.CouplingFilterUtils;
import com.vrbo.jarviz.model.Field;
import com.vrbo.jarviz.model.FieldCoupling;
import com.vrbo.jarviz.model.InheritanceCoupling;
import com.vrbo.jarviz.model.JavaClass;
import com.vrbo.jarviz.model.Method;
import com.vrbo.jarviz.model.MethodCoupling;

public class UsageCollector implements Collector {

    // This default filter will allow all the method couplings without filtering out anything.
    private static CouplingFilterConfig DEFAULT_COUPLING_FILTER = new CouplingFilterConfig.Builder().build();

    private final Multimap<Method, Method> methodRefMap;
    private final Multimap<Method, Field> fieldRefMap;
    private final Multimap<JavaClass, JavaClass> classImplemetationRefMap;
    private final Multimap<JavaClass, JavaClass> classExtensionRefMap;

    private final CouplingFilterConfig couplingFilterConfig;

    public UsageCollector() {
        this(DEFAULT_COUPLING_FILTER);
    }

    /**
     * @param couplingFilterConfig A filter to conditionally select the source and target methods couplings.
     */
    public UsageCollector(final CouplingFilterConfig couplingFilterConfig) {
        Objects.requireNonNull(couplingFilterConfig, "couplingFilterConfig should not be null");

        this.couplingFilterConfig = couplingFilterConfig;
        this.methodRefMap = LinkedHashMultimap.create();
        this.fieldRefMap = LinkedHashMultimap.create();
        this.classImplemetationRefMap = LinkedHashMultimap.create();
        this.classExtensionRefMap = LinkedHashMultimap.create();
    }

    @Override
    public void collectMethodCoupling(final MethodCoupling coupling) {
        if (CouplingFilterUtils.filterMethodCoupling(couplingFilterConfig, coupling)) {
            methodRefMap.put(coupling.getSource(), coupling.getTarget());
        }
    }

    @Override
    public void collectFieldCoupling(FieldCoupling coupling) {
        if (CouplingFilterUtils.filterFieldCoupling(couplingFilterConfig, coupling)) {
            fieldRefMap.put(coupling.getSource(), coupling.getTarget());
        }
    }

    @Override
    public void collectInheritanceCoupling(InheritanceCoupling coupling) {
        switch (coupling.getInheritanceType()) {
            case IMPLEMENTATION:
                collectClassImplementationCoupling(coupling);
                break;
            case EXTENSION:
                collectClassExtensionCoupling(coupling);
                break;
        }
    }

    private void collectClassImplementationCoupling(InheritanceCoupling coupling) {
        if (CouplingFilterUtils.filterInheritanceCoupling(couplingFilterConfig, coupling)) {
            classImplemetationRefMap.put(coupling.getSource(), coupling.getTarget());
        }
    }

    private void collectClassExtensionCoupling(InheritanceCoupling coupling) {
        if (CouplingFilterUtils.filterInheritanceCoupling(couplingFilterConfig, coupling)) {
            classExtensionRefMap.put(coupling.getSource(), coupling.getTarget());
        }
    }

    /**
     * Generates the efferent method coupling graph for each method in the classes loaded by the class loader.
     *
     * @return The list of method couplings.
     */
    public List<MethodCoupling> getMethodCouplings() {
        return ImmutableList.copyOf(
            methodRefMap.entries().stream()
                        .map(UsageCollector::mapToMethodCoupling)
                        .sorted(MethodCoupling.COMPARATOR)
                        .collect(Collectors.toList())
        );
    }

    private static MethodCoupling mapToMethodCoupling(final Entry<Method, Method> entry) {
        return new MethodCoupling.Builder()
                   .source(entry.getKey())
                   .target(entry.getValue())
                   .build();
    }

    /**
     * Generates the efferent field coupling graph for each method in the classes loaded by the class loader.
     *
     * @return The list of method couplings.
     */
    public List<FieldCoupling> getFieldCouplings() {
        return ImmutableList.copyOf(
            fieldRefMap.entries().stream()
                        .map(UsageCollector::mapToFieldCoupling)
                        .sorted(FieldCoupling.COMPARATOR)
                        .collect(Collectors.toList())
        );
    }

    private static FieldCoupling mapToFieldCoupling(final Entry<Method, Field> entry) {
        return new FieldCoupling.Builder()
                   .source(entry.getKey())
                   .target(entry.getValue())
                   .build();
    }

    /**
     * Generates the efferent class inheritance coupling graph for each class loaded by the class loader.
     *
     * @return The list of method couplings.
     */
    public List<InheritanceCoupling> getInheritanceCouplings() {
        return ImmutableList.copyOf(
            Stream.concat(
                classImplemetationRefMap.entries().stream()
                            .map(UsageCollector::mapToClassImplementationCoupling)
                            .sorted(InheritanceCoupling.COMPARATOR),
                classExtensionRefMap.entries().stream()
                        .map(UsageCollector::mapToClassExtensionCoupling)
                        .sorted(InheritanceCoupling.COMPARATOR)
            ).collect(Collectors.toList())
        );
    }

    private static InheritanceCoupling mapToClassImplementationCoupling(final Entry<JavaClass, JavaClass> entry) {
        return new InheritanceCoupling.Builder()
                   .source(entry.getKey())
                   .target(entry.getValue())
                   .inheritanceType(InheritanceCoupling.InheritanceType.IMPLEMENTATION)
                   .build();
    }

    private static InheritanceCoupling mapToClassExtensionCoupling(final Entry<JavaClass, JavaClass> entry) {
        return new InheritanceCoupling.Builder()
                   .source(entry.getKey())
                   .target(entry.getValue())
                   .inheritanceType(InheritanceCoupling.InheritanceType.EXTENSION)
                   .build();
    }
}
