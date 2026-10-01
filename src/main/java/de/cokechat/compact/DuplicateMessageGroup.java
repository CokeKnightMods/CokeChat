package de.cokechat.compact;
/** Metadata only: original message objects remain in vanilla history. */
public record DuplicateMessageGroup(Object key, long startedAt, long lastSeenAt, int count) { }
