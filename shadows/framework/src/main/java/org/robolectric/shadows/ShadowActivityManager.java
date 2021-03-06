package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.pm.ConfigurationInfo;
import android.os.Build.VERSION_CODES;
import android.os.Process;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(ActivityManager.class)
public class ShadowActivityManager {
  private int memoryClass = 16;
  private String backgroundPackage;
  private ActivityManager.MemoryInfo memoryInfo;
  private final List<ActivityManager.RunningTaskInfo> tasks = new CopyOnWriteArrayList<>();
  private final List<ActivityManager.RunningServiceInfo> services = new CopyOnWriteArrayList<>();
  private static List<ActivityManager.RunningAppProcessInfo> processes =
      new CopyOnWriteArrayList<>();
  @RealObject private ActivityManager realObject;
  private Boolean isLowRamDeviceOverride = null;
  private int lockTaskModeState = ActivityManager.LOCK_TASK_MODE_NONE;

  public ShadowActivityManager() {
    ActivityManager.RunningAppProcessInfo processInfo = new ActivityManager.RunningAppProcessInfo();
    fillInProcessInfo(processInfo);
    processInfo.processName = RuntimeEnvironment.application.getPackageName();
    processInfo.pkgList = new String[] {RuntimeEnvironment.application.getPackageName()};
    processes.add(processInfo);
  }

  @Implementation
  public int getMemoryClass() {
    return memoryClass;
  }

  @Implementation
  public static boolean isUserAMonkey() {
    return false;
  }

  @Implementation
  public List<ActivityManager.RunningTaskInfo> getRunningTasks(int maxNum) {
    return tasks;
  }

  @Implementation
  public List<ActivityManager.RunningServiceInfo> getRunningServices(int maxNum) {
    return services;
  }

  @Implementation
  public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
    // This method is explicitly documented not to return an empty list
    if (processes.isEmpty()) {
      return null;
    }
    return processes;
  }

  /** Returns information seeded by {@link #setProcesses}. */
  @Implementation
  protected static void getMyMemoryState(ActivityManager.RunningAppProcessInfo inState) {
    fillInProcessInfo(inState);
    for (ActivityManager.RunningAppProcessInfo info : processes) {
      if (info.pid == Process.myPid()) {
        inState.importance = info.importance;
        inState.lru = info.lru;
        inState.importanceReasonCode = info.importanceReasonCode;
        inState.importanceReasonPid = info.importanceReasonPid;
        inState.lastTrimLevel = info.lastTrimLevel;
        inState.pkgList = info.pkgList;
        inState.processName = info.processName;
      }
    }
  }

  private static void fillInProcessInfo(ActivityManager.RunningAppProcessInfo processInfo) {
    processInfo.pid = Process.myPid();
    processInfo.uid = Process.myUid();
  }

  @Implementation
  public void killBackgroundProcesses(String packageName) {
    backgroundPackage = packageName;
  }

  @Implementation
  public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) {
    if (memoryInfo != null) {
      outInfo.availMem = memoryInfo.availMem;
      outInfo.lowMemory = memoryInfo.lowMemory;
      outInfo.threshold = memoryInfo.threshold;
      outInfo.totalMem = memoryInfo.totalMem;
    }
  }

  @Implementation
  public android.content.pm.ConfigurationInfo getDeviceConfigurationInfo() {
    return new ConfigurationInfo();
  }

  /**
   * @param tasks List of running tasks.
   */
  public void setTasks(List<ActivityManager.RunningTaskInfo> tasks) {
    this.tasks.clear();
    this.tasks.addAll(tasks);
  }

  /**
   * @param services List of running services.
   */
  public void setServices(List<ActivityManager.RunningServiceInfo> services) {
    this.services.clear();
    this.services.addAll(services);
  }

  /**
   * @param processes List of running processes.
   */
  public void setProcesses(List<ActivityManager.RunningAppProcessInfo> processes) {
    this.processes.clear();
    this.processes.addAll(processes);
  }

  /**
   * @return Get the package name of the last background processes killed.
   */
  public String getBackgroundPackage() {
    return backgroundPackage;
  }

  /**
   * @param memoryClass Set the application's memory class.
   */
  public void setMemoryClass(int memoryClass) {
    this.memoryClass = memoryClass;
  }

  /**
   * @param memoryInfo Set the application's memory info.
   */
  public void setMemoryInfo(ActivityManager.MemoryInfo memoryInfo) {
    this.memoryInfo = memoryInfo;
  }

  @Implementation(minSdk = O)
  public static IActivityManager getService() {
    return ReflectionHelpers.createNullProxy(IActivityManager.class);
  }

  @Implementation(minSdk = KITKAT)
  public boolean isLowRamDevice() {
    if (isLowRamDeviceOverride != null) {
      return isLowRamDeviceOverride;
    }
    return directlyOn(realObject, ActivityManager.class, "isLowRamDevice");
  }

  /**
   * Override the return value of isLowRamDevice().
   */
  public ShadowActivityManager setIsLowRamDevice(boolean isLowRamDevice) {
    isLowRamDeviceOverride = isLowRamDevice;
    return this;
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected int getLockTaskModeState() {
    return lockTaskModeState;
  }

  @Implementation(minSdk = VERSION_CODES.LOLLIPOP)
  protected boolean isInLockTaskMode() {
    return getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_NONE;
  }

  /**
   * Sets lock task mode state to be reported by {@link ActivityManager#getLockTaskModeState}, but
   * has no effect otherwise.
   */
  public void setLockTaskModeState(int lockTaskModeState) {
    this.lockTaskModeState = lockTaskModeState;
  }

  @Resetter
  public static void reset() {
    processes.clear();
  }
}
