package com.nynsrulers.lorepowers;

public enum Power {
    // Aelithron's powers:
    VOID_TOTEMS("Void Totems", "Gives the player the ability to bypass totems on kill."),
    BEE_FLIGHT("Bee Flight", "Allows the player to fly like a bee."),
    SPEED_MINE("Speed Mine", "Gives the player infinite Haste 3."), // Dndmastr's power
    PERMANENT_ELYTRA("Permanent Elytra", "Gives the player a permanent, soulbound elytra."), // Exotic Butterfly's power
    GLITCHED_PRESENCE("Glitched Presence", "Only allows the player to be attacked by swords."), // Oli7211's power
    DRAGON_FORM("Dragon Form", "Allows the player to transform into a powerful dragon."), // Power shared between SlothDragon and XxDeathFlamexX.
    //DRAGON_FORM_CARNAGE("Dragon Form Carnage", "Allows the player to destroy a large area while in Dragon Form."), // SlothDragon's power
    // Villagecreep's powers:
    SPECTER_VANISH("Specter Vanish", "Allows the player to enter Spectator Mode for a short period of time."),
    MAP_WARP("Map Warp", "Allows the player to teleport to the center of a map."),
    // Violet_Feather's powers:
    PIGLIN_AVIAN_TRAITS("Piglin-Avian Traits", "Grants the player increased speed, agility, and strength."),
    HEAT_RESISTANCE("Heat Resistance", "Grants the player immunity to fire and magma damage (but not lava)."),
    PIGLIN_AID("Piglin Aid", "Piglins and Piglin Brutes will aggro any attackers to the player and won't attack the player."),
    ANKLE_BITER("Ankle Biter", "Punches dealt by the player have a 10% chance to snap the ankles of the victim, immobilizing them temporarily."),
    // XxDeathFlamexX's powers:
    FIRE_BREATH("Fire Breath", "Allows the player to breathe fire, dealing damage to enemies in front of them.");
    //SHADOW_VANISH("Shadow Vanish", "Allows the player to become invisible for a short period of time."),
    //DRAGON_FORM_WEAPON_BLOCK("Dragon Form Weapon Block", "Allows the player to block attacks from swords, bows, and crossbows when in Dragon Form."),
    //DRAGON_FORM_FIRE_BUFF("Dragon Form Fire Buff", "Increases the damage of the player's fire breath attack in Dragon Form (if the player has fire breath)."),
    //DRAGON_FORM_CRATERS("Dragon Form Craters", "Allows the player to create craters in Dragon Form."),
    //DRAGON_FORM_SPIKES("Dragon Form Spikes", "Allows the player to use the dragon tail spikes as a weapon in Dragon Form.");

    private final String name;
    private final String description;

    Power(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}