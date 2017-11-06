package org.robolectric.android;

import static android.content.res.Configuration.KEYBOARDHIDDEN_UNDEFINED;
import static android.content.res.Configuration.KEYBOARDHIDDEN_YES;
import static android.content.res.Configuration.KEYBOARD_12KEY;
import static android.content.res.Configuration.KEYBOARD_UNDEFINED;
import static android.content.res.Configuration.NAVIGATIONHIDDEN_UNDEFINED;
import static android.content.res.Configuration.NAVIGATIONHIDDEN_YES;
import static android.content.res.Configuration.NAVIGATION_DPAD;
import static android.content.res.Configuration.NAVIGATION_UNDEFINED;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_UNDEFINED;
import static android.content.res.Configuration.SCREENLAYOUT_LAYOUTDIR_LTR;
import static android.content.res.Configuration.SCREENLAYOUT_LAYOUTDIR_MASK;
import static android.content.res.Configuration.SCREENLAYOUT_LAYOUTDIR_RTL;
import static android.content.res.Configuration.SCREENLAYOUT_LONG_MASK;
import static android.content.res.Configuration.SCREENLAYOUT_LONG_UNDEFINED;
import static android.content.res.Configuration.SCREENLAYOUT_LONG_YES;
import static android.content.res.Configuration.SCREENLAYOUT_ROUND_MASK;
import static android.content.res.Configuration.SCREENLAYOUT_ROUND_UNDEFINED;
import static android.content.res.Configuration.SCREENLAYOUT_ROUND_YES;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_UNDEFINED;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE;
import static android.content.res.Configuration.TOUCHSCREEN_NOTOUCH;
import static android.content.res.Configuration.TOUCHSCREEN_UNDEFINED;
import static android.content.res.Configuration.UI_MODE_NIGHT_MASK;
import static android.content.res.Configuration.UI_MODE_NIGHT_UNDEFINED;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;
import static android.content.res.Configuration.UI_MODE_TYPE_APPLIANCE;
import static android.content.res.Configuration.UI_MODE_TYPE_MASK;
import static android.content.res.Configuration.UI_MODE_TYPE_UNDEFINED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import android.content.res.Configuration;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.internal.ParallelUniverse;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;

@RunWith(RobolectricTestRunner.class)
public class BootstrapTest {
  @Test
  public void applySystemConfiguration_shouldAddDefaults() {
    Configuration configuration = new Configuration();
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ParallelUniverse parallelUniverse = new ParallelUniverse();
    parallelUniverse.setSdkConfig(new SdkConfig(RuntimeEnvironment.getApiLevel()));
    String outQualifiers = Bootstrap.applySystemConfiguration("", RuntimeEnvironment.getApiLevel(), configuration, displayMetrics);

    assertThat(outQualifiers).isEqualTo("sw320dp-w320dp-v" + RuntimeEnvironment.getApiLevel());

    assertThat(configuration.mcc).isEqualTo(0);
    assertThat(configuration.mnc).isEqualTo(0);
    assertThat(configuration.locale).isNull();
    assertThat(configuration.smallestScreenWidthDp).isEqualTo(320);
    assertThat(configuration.screenWidthDp).isEqualTo(320);
    assertThat(configuration.screenHeightDp).isEqualTo(0);
    assertThat(configuration.screenLayout & SCREENLAYOUT_SIZE_MASK).isEqualTo(SCREENLAYOUT_SIZE_UNDEFINED);
    assertThat(configuration.screenLayout & SCREENLAYOUT_LONG_MASK).isEqualTo(SCREENLAYOUT_LONG_UNDEFINED);
    assertThat(configuration.screenLayout & SCREENLAYOUT_ROUND_MASK).isEqualTo(SCREENLAYOUT_ROUND_UNDEFINED);
    assertThat(configuration.orientation).isEqualTo(ORIENTATION_UNDEFINED);
    assertThat(configuration.uiMode & UI_MODE_TYPE_MASK).isEqualTo(UI_MODE_TYPE_UNDEFINED);
    assertThat(configuration.uiMode & UI_MODE_NIGHT_MASK).isEqualTo(UI_MODE_NIGHT_UNDEFINED);

    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.JELLY_BEAN) {
      assertThat(configuration.densityDpi).isEqualTo(0);
    }

    assertThat(configuration.touchscreen).isEqualTo(TOUCHSCREEN_UNDEFINED);
    assertThat(configuration.keyboardHidden).isEqualTo(KEYBOARDHIDDEN_UNDEFINED);
    assertThat(configuration.keyboard).isEqualTo(KEYBOARD_UNDEFINED);
    assertThat(configuration.navigationHidden).isEqualTo(NAVIGATIONHIDDEN_UNDEFINED);
    assertThat(configuration.navigation).isEqualTo(NAVIGATION_UNDEFINED);
  }

  @Test
  public void applySystemConfiguration_shouldHonorSpecifiedQualifiers() {
    Configuration configuration = new Configuration();
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ParallelUniverse parallelUniverse = new ParallelUniverse();
    parallelUniverse.setSdkConfig(new SdkConfig(RuntimeEnvironment.getApiLevel()));
    String outQualifiers = Bootstrap.applySystemConfiguration(
        "mcc310-mnc004-fr-rFR-ldrtl-sw400dp-w480dp-h456dp-xlarge-long-round-land-"
            + "appliance-night-hdpi-notouch-keyshidden-12key-navhidden-dpad",
        RuntimeEnvironment.getApiLevel(), configuration, displayMetrics
    );

    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.JELLY_BEAN) {
      // Setting Locale in > JB results in forcing layout direction to match locale
      assertThat(outQualifiers).isEqualTo("mcc310-mnc4-fr-rFR-ldltr-sw400dp-w480dp-h456dp-xlarge"
          + "-long-round-land-appliance-night-hdpi-notouch-keyshidden-12key-navhidden-dpad-v"
          + RuntimeEnvironment.getApiLevel());
    } else {
      assertThat(outQualifiers).isEqualTo("mcc310-mnc4-fr-rFR-ldrtl-sw400dp-w480dp-h456dp-xlarge"
          + "-long-round-land-appliance-night-hdpi-notouch-keyshidden-12key-navhidden-dpad-v"
          + RuntimeEnvironment.getApiLevel());
    }

    assertThat(configuration.mcc).isEqualTo(310);
    assertThat(configuration.mnc).isEqualTo(4);
    assertThat(configuration.locale).isEqualTo(new Locale("fr", "FR"));
    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.JELLY_BEAN) {
      // note that locale overrides ltr/rtl
      assertThat(configuration.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK)
          .isEqualTo(SCREENLAYOUT_LAYOUTDIR_LTR);
    } else {
      // but not on Jelly Bean...
      assertThat(configuration.screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK)
          .isEqualTo(SCREENLAYOUT_LAYOUTDIR_RTL);
    }
    assertThat(configuration.smallestScreenWidthDp).isEqualTo(400);
    assertThat(configuration.screenWidthDp).isEqualTo(480);
    assertThat(configuration.screenHeightDp).isEqualTo(456);
    assertThat(configuration.screenLayout & SCREENLAYOUT_SIZE_MASK).isEqualTo(SCREENLAYOUT_SIZE_XLARGE);
    assertThat(configuration.screenLayout & SCREENLAYOUT_LONG_MASK).isEqualTo(SCREENLAYOUT_LONG_YES);
    assertThat(configuration.screenLayout & SCREENLAYOUT_ROUND_MASK).isEqualTo(SCREENLAYOUT_ROUND_YES);
    assertThat(configuration.orientation).isEqualTo(ORIENTATION_LANDSCAPE);
    assertThat(configuration.uiMode & UI_MODE_TYPE_MASK).isEqualTo(UI_MODE_TYPE_APPLIANCE);
    assertThat(configuration.uiMode & UI_MODE_NIGHT_MASK).isEqualTo(UI_MODE_NIGHT_YES);
    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.JELLY_BEAN) {
      assertThat(configuration.densityDpi).isEqualTo(240);
    }
    assertThat(configuration.touchscreen).isEqualTo(TOUCHSCREEN_NOTOUCH);
    assertThat(configuration.keyboardHidden).isEqualTo(KEYBOARDHIDDEN_YES);
    assertThat(configuration.keyboard).isEqualTo(KEYBOARD_12KEY);
    assertThat(configuration.navigationHidden).isEqualTo(NAVIGATIONHIDDEN_YES);
    assertThat(configuration.navigation).isEqualTo(NAVIGATION_DPAD);
  }

  @Test
  public void applySystemConfiguration_shouldRejectUnknownQualifiers() {
    ParallelUniverse parallelUniverse = new ParallelUniverse();
    parallelUniverse.setSdkConfig(new SdkConfig(RuntimeEnvironment.getApiLevel()));
    try {
      Bootstrap.applySystemConfiguration("notareal-qualifier-sw400dp-w480dp-more-wrong-stuff",
          RuntimeEnvironment.getApiLevel(), new Configuration(), new DisplayMetrics()
      );
      fail("should have thrown");
    } catch (IllegalArgumentException e) {
      // expected
      assertThat(e.getMessage()).contains("notareal");
    }
  }

  @Test
  public void applySystemConfiguration_shouldRejectSdkVersion() {
    ParallelUniverse parallelUniverse = new ParallelUniverse();
    parallelUniverse.setSdkConfig(new SdkConfig(RuntimeEnvironment.getApiLevel()));
    try {
      Bootstrap.applySystemConfiguration("sw400dp-w480dp-v7",
          RuntimeEnvironment.getApiLevel(), new Configuration(), new DisplayMetrics()
      );
      fail("should have thrown");
    } catch (IllegalArgumentException e) {
      // expected
      assertThat(e.getMessage()).contains("Cannot specify platform version");
    }
  }

  @Test
  @Config(sdk = 16)
  public void applySystemConfiguration_densityOnAPI16() {
    Configuration configuration = new Configuration();
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ParallelUniverse parallelUniverse = new ParallelUniverse();
    parallelUniverse.setSdkConfig(new SdkConfig(RuntimeEnvironment.getApiLevel()));
    String outQualifiers = Bootstrap.applySystemConfiguration("hdpi", RuntimeEnvironment.getApiLevel(), configuration, displayMetrics);
    assertThat(displayMetrics.density).isEqualTo(1.5f);
    assertThat(displayMetrics.densityDpi).isEqualTo(240);
  }

}