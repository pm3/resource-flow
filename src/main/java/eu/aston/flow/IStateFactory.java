package eu.aston.flow;

import eu.aston.model.Resource;

@FunctionalInterface
public interface IStateFactory {
    BaseState create(Resource resource, StateStore stateStore);
}
