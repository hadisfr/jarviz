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

import java.util.Optional;
import java.util.regex.Pattern;

import com.vrbo.jarviz.config.CouplingFilterConfig;

public final class CouplingFilterUtils {

    private CouplingFilterUtils() {}

    /**
     * Applying a {@link CouplingFilterConfig} against a given method coupling.
     * Include and exclude filter rules will be applied in conjunction (AND operation).
     * If the return value is true, coupling should be retained or discarded otherwise.
     *
     * @param couplingFilterConfig The filters.
     * @param coupling             The method coupling.
     * @return Indicates whether the given coupling should be kept (true) or discarded (false).
     */
    public static boolean filterMethodCoupling(final CouplingFilterConfig couplingFilterConfig,
                                               final MethodCoupling coupling) {
        return
            couplingFilterConfig.getInclude()
                                .map(f -> matchCoupling(f, coupling))
                                .orElse(true)
            &&
            !couplingFilterConfig.getExclude()
                                 .map(f -> matchCoupling(f, coupling))
                                 .orElse(false);
    }

    /**
     * Applying a {@link CouplingFilterConfig} against a given field coupling.
     * Include and exclude filter rules will be applied in conjunction (AND operation).
     * If the return value is true, coupling should be retained or discarded otherwise.
     *
     * @param couplingFilterConfig The filters.
     * @param coupling             The method coupling.
     * @return Indicates whether the given coupling should be kept (true) or discarded (false).
     */
    public static boolean filterFieldCoupling(final CouplingFilterConfig couplingFilterConfig,
                                               final FieldCoupling coupling) {
        // TODO implement filters
        return true;
    }

    /**
     * Matches a given coupling to a RegEx backed {@link CouplingFilter}.
     *
     * @param filter   The filter.
     * @param coupling The coupling.
     * @return Indicates whether the given coupling should be kept (true) or discarded (false).
     */
    static boolean matchCoupling(final CouplingFilter filter, final MethodCoupling coupling) {
        return matchString(filter.getSourcePackagePattern(), coupling.getSource().getPackageName()) &&
               matchString(filter.getSourceClassPattern(), coupling.getSource().getSimpleClassName()) &&
               matchString(filter.getSourceMethodPattern(), coupling.getSource().getMethodName()) &&
               matchString(filter.getTargetPackagePattern(), coupling.getTarget().getPackageName()) &&
               matchString(filter.getTargetClassPattern(), coupling.getTarget().getSimpleClassName()) &&
               matchString(filter.getTargetMethodPattern(), coupling.getTarget().getMethodName());
    }

    /**
     * Matches a given string to an optional RegEx pattern.
     * If pattern is missing, this will consider it as matched, thus returning true.
     *
     * @param pattern The RegEx pattern
     * @param string  The string to match to the given pattern.
     * @return Indicates whether the given coupling should be kept (true) or discarded (false).
     */
    static boolean matchString(final Optional<Pattern> pattern, final String string) {
        return pattern.map(p -> p.matcher(string).matches())
                      .orElse(true); // If pattern is not available, it is considered a match (pass through)

    }
}
