package io.github.thebusybiscuit.exoticgarden;

public enum PlantType {

	BUSH("BUSH"),
	FRUIT("FRUIT"),
	DOUBLE_PLANT("DOUBLE_FRUIT"),
	ORE_PLANT("ORE_PLANT");
	final String type;
	PlantType(String type) {
		this.type = type;
	}

	public String toString() {
		return type;
	}

	public static PlantType fromString(String type) {
		return PlantType.valueOf(type.toUpperCase());
	}

}
