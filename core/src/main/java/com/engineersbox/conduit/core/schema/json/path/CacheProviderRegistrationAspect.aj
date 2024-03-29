package com.engineersbox.conduit.core.schema.json.path;

import com.engineersbox.conduit.core.processing.task.worker.ClientBoundForkJoinWorkerThead;
import com.jayway.jsonpath.spi.cache.Cache;

public aspect CacheProviderRegistrationAspect {

    pointcut cacheProviderInstantiationVisit():
            execution(public static com.jayway.jsonpath.spi.cache.Cache com.jayway.com.jayway.jsonpath.spi.cache.CacheProvider.getCache());

    Cache around(): cacheProviderInstantiationVisit() {
        final Cache cache;
        if (!(Thread.currentThread() instanceof ClientBoundForkJoinWorkerThead cbfjwThread)
            || (cache = AffinityCacheProvider.getCacheInstance(cbfjwThread.getAffinityId())) == null) {
            return proceed();
        }
        return cache;
    }
    // -javaagent:$MAVEN_REPOSITORY$/org/aspectj/aspectjweaver/1.9.19/aspectjweaver-1.9.19.jar
}
