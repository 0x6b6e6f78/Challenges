package de.corey.challenges.model.lists;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.stream.Stream;

public class MaterialStringList extends StringList<Material> {

    public MaterialStringList(int limit) {
        super(limit);
    }

    @Override
    public Stream<String> specification() {
        return Arrays.stream(Material.values()).map(Material::name);
    }

    @Override
    public Stream<Material> tStream() {
        return stream().map(Material::valueOf);
    }
}
