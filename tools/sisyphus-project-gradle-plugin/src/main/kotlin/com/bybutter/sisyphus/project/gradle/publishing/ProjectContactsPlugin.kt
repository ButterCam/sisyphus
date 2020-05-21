package com.bybutter.sisyphus.project.gradle.publishing

import com.bybutter.sisyphus.project.gradle.ensurePlugin
import com.bybutter.sisyphus.project.gradle.tryApplyPluginClass
import nebula.plugin.contacts.ContactsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ProjectContactsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.ensurePlugin("nebula.maven-base-publish", ::apply) {
            return
        }
        try {
            Class.forName("nebula.plugin.contacts.ContactsExtension")
        }catch (ex: ClassNotFoundException) {
            return
        }
        val rootContacts = target.rootProject.extensions.findByType(ContactsExtension::class.java) ?: return
        target.tryApplyPluginClass("nebula.plugin.contacts.ContactsPlugin")
        if (target != target.rootProject) {
            val currentContacts = target.extensions.getByType(ContactsExtension::class.java)

            for ((email, person) in rootContacts.people) {
                if (!currentContacts.people.containsKey(email)) {
                    currentContacts.people[email] = person
                }
            }
        }
    }
}