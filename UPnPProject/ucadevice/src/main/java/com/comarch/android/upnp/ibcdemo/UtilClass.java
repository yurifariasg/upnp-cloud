package com.comarch.android.upnp.ibcdemo;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.LocalDevice;

import java.util.ArrayList;

/**
 * Created by yurifariasg on 17/07/16.
 */
public class UtilClass {

    public static AndroidUpnpService upnpService;

    public static LocalDevice MainDevice;

    public static ArrayList<Long> mStartXMPPTimes = new ArrayList<>();

    public static ArrayList<Long> mDisconnectXMPPTimes = new ArrayList<>();

}
