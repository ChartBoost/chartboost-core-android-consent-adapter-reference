/*
 * Copyright 2024 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

rootProject.name = "ReferenceConsentAdapter"
include(":ReferenceConsentAdapter")
include(":chartboostcore")
include(":ChartboostCore")
