/*
 * Copyright (c) 2020 Proton Technologies AG
 *
 * This file is part of ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */
package ch.protonmail.android.domain.entity.user

import ch.protonmail.android.domain.entity.Id
import ch.protonmail.android.domain.entity.PgpField
import ch.protonmail.android.domain.entity.Validable
import ch.protonmail.android.domain.entity.Validated
import ch.protonmail.android.domain.entity.Validator
import ch.protonmail.android.domain.entity.requireValid

// It is possible for an address to not have any key

/**
 * Representation of an user's address' Key
 * @author Davide Farella
 */
@Validated // TODO more fine grained validation regarding activation, token and signature. Must be evaluated when the
//             business logic will receive some refactor there
data class AddressKey(
    val id: Id,
    val version: UInt,
    /**
     * Represented on BE as 'Flags' 2
     * It is `true` is Flags is 2 or 3 ( 2 + 1 )
     */
    val canEncrypt: Boolean,
    /**
     * Represented on BE as 'Flags' 1
     * It is `true` is Flags is 1 or 3 ( 1 + 2 )
     */
    val canVerifySignature: Boolean,
    val publicKey: PgpField.PublicKey,
    val privateKey: PgpField.PrivateKey,
    /**
     * Used for activation
     * @see activation
     */
    val token: PgpField.Message?,
    /**
     * Used for activation
     * @see activation
     */
    val signature: PgpField.Signature?,
    /**
     * This is used when bootstrapping a new address key generated by an admin for a member in an organization context.
     * As the admin does not know the member password, the admin will encrypt the new address key using a random token.
     * This token will then be encrypted using the member’s user key.
     *
     * At the first login the address key will be ‘activated’ aka the token will be decrypted, which will be used to
     * decrypt the encrypted address key.
     * The address key will be encrypted using the pwd + salt and updated.
     */
    val activation: PgpField.Message?,

    /**
     * Newly added entry in AddressKey model on API side. Designates whether a key can be decrypted or not.
     * Non-decryptable keys should be excluded from keylists since their ownership cannot be proved
     */
    val active: Boolean
) {

    /**
     * Create value of 'Flags' for backend
     */
    fun buildBackEndFlags() =
        (if (canEncrypt) 2 else 0) + if (canVerifySignature) 1 else 0
}

/**
 * A set of [AddressKey]s with a primary one
 * [Validable]: [keys] must contains [primaryKey], if not `null`
 *
 * @param primaryKey can be `null`, as an [Address] is not required to have keys
 * @param keys can be empty only if [primaryKey] is `null`
 */
@Validated
data class AddressKeys(
    val primaryKey: AddressKey?,
    val keys: Collection<AddressKey>
) : Validable by Validator<AddressKeys>({
        require(primaryKey == null && keys.isEmpty() || primaryKey in keys)
    }) {
    init { requireValid() }

    val hasKeys get() = keys.isNotEmpty()

    companion object {

        /**
         * Empty [AddressKeys]
         */
        val Empty get() = AddressKeys(null, emptySet())
    }
}
