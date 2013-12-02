package com.andresjesse.jpctblend;

/**
 * Class designed to provide default builders for Actors
 * @author andres
 *
 */
public class ActorFactory {
	
	private static final String actorsPackage = "com.andresjesse.jpctblend.actors";

	private static ActorFactory instance;

	/**
	 * Private constructor, to avoid the use of "new" operator
	 */
	private ActorFactory() {
	}

	public static ActorFactory getInstance() {
		if (instance == null)
			instance = new ActorFactory();
		return instance;
	}

	/**
	 * Create an Actor based on his java class name.
	 * @param actorClass java class that implements IActor
	 * @return the actor
	 */
	public IActor createFromString(String actorClass) {

		try {
			Object unknownActor = Class.forName(actorsPackage+"."+actorClass).newInstance();

			if (unknownActor instanceof IActor) {
				System.out.println("JPCTBlend: create actor: "+actorsPackage+"."+actorClass);
				return (IActor)unknownActor;
			} else {
				throw new RuntimeException(
						"JPCTBlend error: Can't create actor, class "
								+ actorsPackage+"."+actorClass
								+ " must implement IActor interface!");
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
					"JPCTBlend error: Can't create actor, class ''" + actorsPackage+"."+actorClass
							+ "'' not found!\n Did you forgot to create it?");
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}
}
