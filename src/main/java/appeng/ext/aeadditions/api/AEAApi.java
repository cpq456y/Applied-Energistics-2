package appeng.ext.aeadditions.api;

public class AEAApi {

	public static IAEAdditionsAPI instance() {
		if (instance == null) {
			try {
				instance = (IAEAdditionsAPI) Class
					.forName("appeng.ext.aeadditions.AEAdditionsApi")
					.getField("instance").get(null);
			} catch (Exception e) {
			}
		}
		return instance;
	}

	private static IAEAdditionsAPI instance = null;

}
