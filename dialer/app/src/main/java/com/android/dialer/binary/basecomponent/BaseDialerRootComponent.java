/*
 * Copyright (C) 2017 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.dialer.binary.basecomponent;

import com.android.bubble.BubbleComponent;
import com.android.dialer.common.activecalls.ActiveCallsComponent;
import com.android.dialer.calllog.CallLogComponent;
import com.android.dialer.common.calllog.config.CallLogConfigComponent;
import com.android.dialer.calllog.database.CallLogDatabaseComponent;
import com.android.dialer.calllog.ui.CallLogUiComponent;
import com.android.dialer.commandline.CommandLineComponent;
import com.android.dialer.common.concurrent.DialerExecutorComponent;
import com.android.dialer.common.configprovider.ConfigProviderComponent;
import com.android.dialer.common.contacts.ContactsComponent;
import com.android.dialer.common.duo.DuoComponent;
import com.android.dialer.common.enrichedcall.EnrichedCallComponent;
import com.android.incallui.feedback.FeedbackComponent;
import com.android.dialer.common.glidephotomanager.GlidePhotoManagerComponent;
import com.android.dialer.common.metrics.MetricsComponent;
import com.android.dialer.common.phonelookup.PhoneLookupComponent;
import com.android.dialer.common.phonelookup.database.PhoneLookupDatabaseComponent;
import com.android.dialer.common.phonenumbergeoutil.PhoneNumberGeoUtilComponent;
import com.android.dialer.common.precall.PreCallComponent;
import com.android.dialer.common.preferredsim.PreferredSimComponent;
import com.android.dialer.common.preferredsim.suggestion.SimSuggestionComponent;
import com.android.dialer.common.promotion.PromotionComponent;
import com.android.dialer.simulator.SimulatorComponent;
import com.android.dialer.common.spam.SpamComponent;
import com.android.contacts.common.speeddial.loader.UiItemLoaderComponent;
import com.android.dialer.common.storage.StorageComponent;
import com.android.dialer.common.strictmode.StrictModeComponent;
import com.android.dialer.common.theme.base.ThemeComponent;
import com.android.incallui.calllocation.CallLocationComponent;
import com.android.incallui.maps.MapsComponent;
import com.android.incallui.speakeasy.SpeakEasyComponent;
import com.android.voicemail.VoicemailComponent;

/**
 * Base class for the core application-wide component. All variants of the Dialer app should extend
 * from this component.
 */
public interface BaseDialerRootComponent
    extends ActiveCallsComponent.HasComponent,
        BubbleComponent.HasComponent,
        CallLocationComponent.HasComponent,
        CallLogComponent.HasComponent,
        CallLogConfigComponent.HasComponent,
        CallLogDatabaseComponent.HasComponent,
        CallLogUiComponent.HasComponent,
        ConfigProviderComponent.HasComponent,
        CommandLineComponent.HasComponent,
        ContactsComponent.HasComponent,
        DialerExecutorComponent.HasComponent,
        DuoComponent.HasComponent,
        EnrichedCallComponent.HasComponent,
        FeedbackComponent.HasComponent,
        GlidePhotoManagerComponent.HasComponent,
        MapsComponent.HasComponent,
        MetricsComponent.HasComponent,
        PhoneLookupComponent.HasComponent,
        PhoneLookupDatabaseComponent.HasComponent,
        PhoneNumberGeoUtilComponent.HasComponent,
        PreCallComponent.HasComponent,
        PreferredSimComponent.HasComponent,
        PromotionComponent.HasComponent,
        UiItemLoaderComponent.HasComponent,
        SimSuggestionComponent.HasComponent,
        SimulatorComponent.HasComponent,
        SpamComponent.HasComponent,
        SpeakEasyComponent.HasComponent,
        StorageComponent.HasComponent,
        StrictModeComponent.HasComponent,
        ThemeComponent.HasComponent,
        VoicemailComponent.HasComponent {}
