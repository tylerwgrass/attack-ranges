package com.attackranges.weapons;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public class WeaponsGenerator {
    private static final WeaponIds weaponIds = new WeaponIds();
    public static Map<Integer, Weapon> generate() {
        final Map<Integer, Weapon> standardWeapons = generateStandard(weaponIds.getStandardWeapons());
        final Map<Integer, Weapon> nonStandardWeapons = generateNonstandard(weaponIds.getNonStandardWeapons());
        final Map<Integer, Weapon> weapons = new HashMap<>();
        weapons.putAll(standardWeapons);
        weapons.putAll(nonStandardWeapons);
        return weapons;
    }

    private static Map<Integer, Weapon> generateStandard(
            Set<Pair<Set<Integer>, Function<Integer, Weapon>>> standardWeapons) {
        final Map<Integer, Weapon> weapons = new HashMap<>();
        for(Pair<Set<Integer>, Function<Integer, Weapon>> idsConstructorPair : standardWeapons) {
            for (Integer weaponId : idsConstructorPair.getLeft()) {
                Weapon weapon = idsConstructorPair.getRight().apply(weaponId);
                if (weapon != null) {
                    weapons.put(weapon.id, weapon);
                }
            }
        }
        return weapons;
    }

    private static Map<Integer, Weapon> generateNonstandard(
            Set<Pair<Set<List<Integer>>, Function<List<Integer>, Weapon>>> nonStandardWeapons) {
        final Map<Integer, Weapon> weapons = new HashMap<>();
        for (Pair<Set<List<Integer>>, Function<List<Integer>, Weapon>> idsConstructorPair : nonStandardWeapons) {
            for(List<Integer> weaponData : idsConstructorPair.getLeft()) {
                Weapon weapon = idsConstructorPair.getRight().apply(weaponData);
                if (weapon != null) {
                    weapons.put(weapon.id, weapon);
                }
            }
        }
        return weapons;
    }
}
