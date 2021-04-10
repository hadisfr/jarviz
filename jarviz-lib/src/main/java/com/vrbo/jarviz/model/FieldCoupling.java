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
@JsonSerialize(as = ImmutableFieldCoupling.class)
@JsonDeserialize(as = ImmutableFieldCoupling.class)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public interface FieldCoupling {

    Comparator<FieldCoupling> COMPARATOR = (a, b) -> {
        final int comparison = Method.COMPARATOR.compare(a.getSource(), b.getSource());
        if (comparison == 0) {
            return Field.COMPARATOR.compare(a.getTarget(), b.getTarget());
        }
        return comparison;
    };

    /**
     * Source method
     *
     * @return The source method.
     */
    Method getSource();

    /**
     * Target field
     *
     * @return The target field.
     */
    Field getTarget();

    class Builder extends ImmutableFieldCoupling.Builder {}
}
