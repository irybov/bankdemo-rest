package com.github.irybov.bankdemorest.mapper;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;

public class CycleAvoidingMappingContext {
	
    private Map<Object, Object> knownInstances = new IdentityHashMap<>();
//    private Map<List<Object>, List<Object>> knownLists = new IdentityHashMap<>();

    @BeforeMapping
    public <T> T getMappedInstance(Object source, @TargetType Class<T> targetType) {
        return targetType.cast(knownInstances.get(source));
    }
    @BeforeMapping
    public void storeMappedInstance(Object source, @MappingTarget Object target) {
        knownInstances.put(source, target);
    }
/*    
    @BeforeMapping
    public <T> List<T> getMappedList(List<Object> source, @TargetType Class<List<T>> targetType) {
        return targetType.cast(knownLists.get(source));
    }
    @BeforeMapping
    public void storeMappedList(List<Object> source, @MappingTarget List<Object> target) {
    	knownLists.put(source, target);
    }
*/
}
