package org.dev_alex.mojo_qa.mojo.custom_views.camera;

import android.os.Bundle;
import androidx.annotation.Nullable;

/**
 * Very short version of presenter, mandatory methods so presenter knows when to start and stop
 */
public interface PresenterShortLifecycle {

    void onCreate(@Nullable Bundle saveState);

    void onDestroy();
}
