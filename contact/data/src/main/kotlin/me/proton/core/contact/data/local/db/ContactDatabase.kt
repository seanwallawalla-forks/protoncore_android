/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.contact.data.local.db

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.proton.core.contact.data.local.db.dao.ContactCardDao
import me.proton.core.contact.data.local.db.dao.ContactDao
import me.proton.core.contact.data.local.db.dao.ContactEmailDao
import me.proton.core.contact.data.local.db.dao.ContactEmailLabelCrossRefDao
import me.proton.core.contact.data.local.db.entity.toContact
import me.proton.core.contact.data.local.db.entity.toContactCardEntity
import me.proton.core.contact.data.local.db.entity.toContactEmailEntity
import me.proton.core.contact.data.local.db.entity.toContactEmailLabelCrossRefs
import me.proton.core.contact.data.local.db.entity.toContactEntity
import me.proton.core.contact.data.local.db.entity.toContactWithCards
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.domain.entity.UserId

interface ContactDatabase: Database {
    fun contactDao(): ContactDao
    fun contactCardDao(): ContactCardDao
    fun contactEmailDao(): ContactEmailDao
    fun contactEmailLabelDao(): ContactEmailLabelCrossRefDao

    fun getContact(contactId: ContactId): Flow<ContactWithCards> {
        return contactDao().getContact(contactId).map { it.toContactWithCards() }
    }

    fun getAllContacts(userId: UserId): Flow<List<Contact>> {
        return contactDao().getAllContacts(userId).map { entities ->
            entities.map { it.toContact() }
        }.distinctUntilChanged()
    }

    suspend fun mergeContacts(userId: UserId, contacts: List<Contact>) {
        inTransaction {
            contactDao().deleteAllContacts(userId)
            contacts.forEach { mergeContact(userId, it) }
        }
    }

    suspend fun mergeContact(userId: UserId, contact: Contact) {
        inTransaction {
            contactDao().insertOrUpdate(contact.toContactEntity(userId))

            contactEmailDao().deleteAllContactsEmails(contact.id)
            mergeContactEmails(userId, contact.contactEmails)
        }
    }

    suspend fun mergeContactWithCards(userId: UserId, contactWithCards: ContactWithCards) {
        inTransaction {
            mergeContact(userId, contactWithCards.contact)

            contactCardDao().deleteAllContactCards(contactWithCards.id)
            val contactCardsEntities = contactWithCards.contactCards.map { it.toContactCardEntity(contactWithCards.id) }
            contactCardDao().insertOrUpdate(*contactCardsEntities.toTypedArray())
        }
    }

    suspend fun mergeContactEmails(userId: UserId, contactsEmails: List<ContactEmail>) {
        inTransaction {
            contactEmailLabelDao().deleteAllLabels(contactsEmails.map { it.id })

            val mailEntities = contactsEmails.map { it.toContactEmailEntity(userId) }
            contactEmailDao().insertOrUpdate(*mailEntities.toTypedArray())

            val mailLabelEntities = contactsEmails.flatMap { it.toContactEmailLabelCrossRefs() }
            contactEmailLabelDao().insertOrUpdate(*mailLabelEntities.toTypedArray())
        }
    }

    companion object {
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                TODO("provide migration when feature done")
            }
        }
    }
}
