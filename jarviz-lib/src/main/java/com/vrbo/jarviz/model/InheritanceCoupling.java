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

package com.vrbo.jarviz.model;

import java.util.Comparator;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class represents a coupling between two fields.
 * We should be able to represent the majority of the
 * {@link java.lang.invoke.CallSite}s as couplings.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableInheritanceCoupling.class)
@JsonDeserialize(as = ImmutableInheritanceCoupling.class)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public interface InheritanceCoupling {

    Comparator<InheritanceCoupling> COMPARATOR = (a, b) -> {
        final int comparison = JavaClass.COMPARATOR.compare(a.getSource(), b.getSource());
        if (comparison == 0) {
            return JavaClass.COMPARATOR.compare(a.getTarget(), b.getTarget());
        }
        return comparison;
    };

    /**
     * Source method
     *
     * @return The sub class.
     */
    JavaClass getSource();

    /**
     * Target field
     *
     * @return The super class.
     */
    JavaClass getTarget();

    /**
     * Inheritance type
     *
     * @return The inheritance type.
     */
    InheritanceType getInheritanceType();

    public enum InheritanceType {
        IMPLEMENTATION,
        EXTENSION;
    }

    class Builder extends ImmutableInheritanceCoupling.Builder {}
}
