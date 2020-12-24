package org.dev_alex.mojo_qa.mojo.models


class OrgUser(
        val config: String,
        val description: String,
        val email: String,
        val firstName: String,
        val fullname: String,
        val has_avatar: Boolean,
        val id: Int,
        val is_deleted: Boolean,
        val is_orgowner: Boolean,
        val langcode: String,
        val lastName: String,
        val push_disabled: Boolean,
        val username: String
) {
    override fun toString(): String {
        return firstName.takeIf { it.isNotBlank() } ?: fullname
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrgUser) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }и
}