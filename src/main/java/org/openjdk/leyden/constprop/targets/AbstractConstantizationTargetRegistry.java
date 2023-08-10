package org.openjdk.leyden.constprop.targets;

import java.util.Collection;

public abstract class AbstractConstantizationTargetRegistry {
    public abstract Collection<ConstantizationTarget> getClassScopeTargets(String clazz);

    public abstract Collection<ConstantizationTarget> getMethodScopeTargets(String clazz, String method, String descriptor);
}
