package vazkii.quark.base.module;

import net.minecraftforge.fml.loading.FMLEnvironment;

public enum SubscriptionTarget {

	BOTH_SIDES(true, true), 
	CLIENT_ONLY(true, false), 
	SERVER_ONLY(false, true), 
	NONE(false, false);
	
	private SubscriptionTarget(boolean client, boolean server) {
		this.client = client;
		this.server = server;
	}
	
	private boolean client, server;
	
	public boolean shouldSubscribe() {
		return FMLEnvironment.dist.isClient() ? client : server;
	}
	
	public static SubscriptionTarget fromString(String s) {
		for(SubscriptionTarget target : values())
			if(target.name().equals(s))
				return target;
		
		return null;
	}
	
	
}
