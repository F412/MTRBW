package mtrbw.app;

public class AppConfig {
	// Server user login url
	public static String URL_LOGIN = "xxx"; // http://www.your-site.com/android_login_api/

	// Server user register url
	public static String URL_REGISTER = "xxx"; // http://www.your-site.com/android_login_api/

    //Determine whether Locations are only recorded within a predetermined area
    public static boolean limitedArea = false;

    //Center point
    public static double centerLat = 50.885466;
    public static double centerLon = 6.891250;

    //Radius in meters
    public static double radius = 500;
}
