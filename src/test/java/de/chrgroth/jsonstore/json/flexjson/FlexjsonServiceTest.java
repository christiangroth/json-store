package de.chrgroth.jsonstore.json.flexjson;

import de.chrgroth.jsonstore.JsonService;
import de.chrgroth.jsonstore.json.AbstractJsonServiceTest;

public class FlexjsonServiceTest extends AbstractJsonServiceTest {

    @Override
    protected JsonService createJsonService() {
        return FlexjsonService.builder().setPrettyPrint(true).setDeepSerialize(true).build();
    }
}
