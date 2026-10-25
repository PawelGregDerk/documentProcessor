package by.vstu.isit.documentprocessor.entities;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractEntityTest {

    @SuperBuilder
    @NoArgsConstructor
    static class TestEntity extends AbstractEntity<Long> {
    }

    @Test
    @DisplayName("id getter and setter: correctly store and return id")
    void id_getterAndSetter_worksCorrectly() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getId());

        entity.setId(42L);
        assertEquals(42L, entity.getId());
    }

    @Test
    @DisplayName("equals and hashCode: entities with same id are equal regardless of instance")
    void equalsAndHashCode_sameId_areEqual() {
        TestEntity e1 = TestEntity.builder().id(100L).build();
        TestEntity e2 = TestEntity.builder().id(100L).build();
        TestEntity e3 = TestEntity.builder().id(200L).build();

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
        assertNotEquals(e1, null);
        assertNotEquals(e1, new Object());
    }

    @Test
    @DisplayName("toString: includes class name and id")
    void toString_includesId() {
        TestEntity entity = TestEntity.builder().id(77L).build();
        String str = entity.toString();

        assertTrue(str.contains("id=77"));
    }
}
