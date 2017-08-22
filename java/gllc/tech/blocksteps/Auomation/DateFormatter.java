package gllc.tech.blocksteps.Auomation;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bhangoo on 8/22/2017.
 */

public class DateFormatter {
    public static String GetConCatDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, day);
        SimpleDateFormat format = new SimpleDateFormat("MMddyy");
        String formattedDate = format.format(calendar.getTime());
        if (formattedDate.length() == 6) {formattedDate = formattedDate.substring(1);}

        return formattedDate;
    }

    public static String getHourlyTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm - MM/dd/yy");
        String formattedDate = format.format(calendar.getTime());

        return formattedDate;
    }
}
