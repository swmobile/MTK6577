/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef _VOLUMEMANAGER_H
#define _VOLUMEMANAGER_H

#include <pthread.h>

#ifdef __cplusplus
#include <utils/List.h>
#include <sysutils/SocketListener.h>
#include <linux/spinlock_types.h>
#include <linux/spinlock.h>
#include <cutils/properties.h>

#include "Volume.h"
#include "CFG_OMADMUSB_File.h"

/* The length of an MD5 hash when encoded into ASCII hex characters */
#define MD5_ASCII_LENGTH_PLUS_NULL ((MD5_DIGEST_LENGTH*2)+1)

typedef enum { ASEC, OBB } container_type_t;

class ContainerData {
public:
    ContainerData(char* _id, container_type_t _type)
            : id(_id)
            , type(_type)
    {}

    ~ContainerData() {
        if (id != NULL) {
            free(id);
            id = NULL;
        }
    }

    char *id;
    container_type_t type;
};

typedef android::List<ContainerData*> AsecIdCollection;

class VolumeManager {
private:
    static VolumeManager *sInstance;

private:
    SocketListener        *mBroadcaster;

    VolumeCollection      *mVolumes;
    AsecIdCollection      *mActiveContainers;
    bool				   mUseBackupContainers;

    bool                   mDebug;

    // for adjusting /proc/sys/vm/dirty_ratio when UMS is active
    int                    mUmsSharingCount;
    int                    mSavedDirtyRatio;
    int                    mUmsDirtyRatio;
    int                    mVolManagerDisabled;
	bool                   mIsFirstBoot;
#ifdef MTK_2SDCARD_SWAP
    bool                   mIs2sdSwapped;
    bool                   bSdCardSwapBootComplete;
	bool						bNeedSwapAfterUnmount ;
	bool						bNeedSwapAfterMount ;
	spinlock_t mUpdateSwapPropertyLock;
#endif

public:
    bool				   mRetrunEmptyContainersForOnce;
    virtual ~VolumeManager();

    int start();
    int stop();

    void handleBlockEvent(NetlinkEvent *evt);

    int addVolume(Volume *v);

    int listVolumes(SocketClient *cli);
    int mountVolume(const char *label);
    int unmountVolume(const char *label, bool force, bool revert);
    int shareVolume(const char *label, const char *method);
    //M{
    int USBEnable(bool enable);
	static int NvramAccessForOMADM(OMADMUSB_CFG_Struct *st, bool isRead);
    //}M
    int unshareVolume(const char *label, const char *method);
    int shareEnabled(const char *path, const char *method, bool *enabled);
    int formatVolume(const char *label);
    void disableVolumeManager(void) { mVolManagerDisabled = 1; }

    /* ASEC */
    int listBackupAsec(SocketClient *cli);
    int waitForAfCleanupAsec(Volume *v);

    int createAsec(const char *id, unsigned numSectors, const char *fstype,
                   const char *key, int ownerUid);
    int finalizeAsec(const char *id);
    int destroyAsec(const char *id, bool force);
    int mountAsec(const char *id, const char *key, int ownerUid);
    int unmountAsec(const char *id, bool force);
    int renameAsec(const char *id1, const char *id2);
    int getAsecMountPath(const char *id, char *buffer, int maxlen);
    int getAsecFilesystemPath(const char *id, char *buffer, int maxlen);

    /* Loopback images */
    int listMountedObbs(SocketClient* cli);
    int mountObb(const char *fileName, const char *key, int ownerUid);
    int unmountObb(const char *fileName, bool force);
    int getObbMountPath(const char *id, char *buffer, int maxlen);

    /* Shared between ASEC and Loopback images */
    int unmountLoopImage(const char *containerId, const char *loopId,
            const char *fileName, const char *mountPoint, bool force);

    void setDebug(bool enable);

    // XXX: Post froyo this should be moved and cleaned up
    int cleanupAsec(Volume *v, bool force);

    void setBroadcaster(SocketListener *sl) { mBroadcaster = sl; }
    SocketListener *getBroadcaster() { return mBroadcaster; }

    static VolumeManager *Instance();

    static char *asecHash(const char *id, char *buffer, size_t len);

    Volume *lookupVolume(const char *label);
    int getNumDirectVolumes(void);
    int getDirectVolumeList(struct volume_info *vol_list);
	bool isFirstBoot() {return mIsFirstBoot;}
	void setIpoState(int state){mIpoState=state;}
	int getIpoState(void){return mIpoState;}
    static const int State_Ipo_Shutdown    = 1;
    static const int State_Ipo_Start       = 2;
#ifdef MTK_SD_REINIT_SUPPORT
	int reinitExternalSD();
#endif

#ifdef MTK_SHARED_SDCARD
    void setSharedSdState(int state);
#endif

#ifdef MTK_2SDCARD_SWAP
	void swap2Sdcard();
	bool is2SdcardSwapped() {return mIs2sdSwapped;};
	void setNeedSwapAfterUnmount(bool value) { bNeedSwapAfterUnmount = value ;} ;
	bool getNeedSwapAfterUnmount(void) {return bNeedSwapAfterUnmount;} ;
	void setNeedSwapAfterMount(bool value) { bNeedSwapAfterMount = value ;} ;
	bool getNeedSwapAfterMount(void) {return bNeedSwapAfterMount;} ;
	void set2SdcardSwapped(bool isSwapped) {		
		spin_lock(&mUpdateSwapPropertyLock) ;
		mIs2sdSwapped = isSwapped ;
		if(mIs2sdSwapped) {
			property_set("internal_sd_path", "/mnt/sdcard2");
			property_set("external_sd_path", "/mnt/sdcard");
		}
		else {
			property_set("internal_sd_path", "/mnt/sdcard");
			property_set("external_sd_path", "/mnt/sdcard2");
		}
		spin_unlock(&mUpdateSwapPropertyLock) ;
	}
#endif
	void mountallVolumes();

private:
    VolumeManager();
    int mIpoState;
    void readInitialState();
    bool isMountpointMounted(const char *mp);
};

extern "C" {
#endif /* __cplusplus */
#define UNMOUNT_NOT_MOUNTED_ERR -2
    int vold_disableVol(const char *label);
    int vold_getNumDirectVolumes(void);
    int vold_getDirectVolumeList(struct volume_info *v);
#ifdef __cplusplus
}
#endif

#endif
