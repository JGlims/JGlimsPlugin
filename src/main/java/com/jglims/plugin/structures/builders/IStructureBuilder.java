package com.jglims.plugin.structures.builders;

import com.jglims.plugin.structures.StructureBuilder;

/**
 * Marker interface for one-class-per-structure builders.
 *
 * <p>Each concrete structure (e.g. {@code AncientTempleBuilder}) implements this
 * interface and performs its block placement through the provided
 * {@link StructureBuilder} primitive library. This mirrors the
 * {@link com.jglims.plugin.abyss.AbyssCitadelBuilder} separation pattern but at
 * the smaller, per-structure granularity the user requested.
 *
 * <p>Builder classes are stateless and cheap to construct — {@link StructureRegistry}
 * instantiates each at startup and dispatches {@link com.jglims.plugin.structures.StructureType}
 * entries to them via {@link #build(StructureBuilder)}.
 */
public interface IStructureBuilder {

    /**
     * Places every block for this structure, relative to the origin of {@code b}.
     *
     * <p>Implementations MUST:
     * <ul>
     *   <li>use only relative coordinates through the {@code b} primitives,</li>
     *   <li>place at least one chest via {@code b.placeChest(...)} when loot is expected,</li>
     *   <li>call {@code b.setBossSpawn(...)} when the structure has a boss,</li>
     *   <li>never hold per-instance state — every call is independent.</li>
     * </ul>
     */
    void build(StructureBuilder b);
}
