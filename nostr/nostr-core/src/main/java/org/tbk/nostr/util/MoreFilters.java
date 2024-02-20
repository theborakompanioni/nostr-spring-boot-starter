package org.tbk.nostr.util;

import com.google.protobuf.Descriptors;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.TagValue;

import java.util.List;
import java.util.Optional;

public final class MoreFilters {
    private static final Descriptors.FieldDescriptor SINCE_FIELD = Filter.getDescriptor().findFieldByNumber(Filter.SINCE_FIELD_NUMBER);
    private static final Descriptors.FieldDescriptor UNTIL_FIELD = Filter.getDescriptor().findFieldByNumber(Filter.UNTIL_FIELD_NUMBER);

    private MoreFilters() {
        throw new UnsupportedOperationException();
    }

    public static boolean matches(Event event, List<Filter> filtersList) {
        return filtersList.stream().anyMatch(it -> matches(event, it));
    }

    public static boolean matches(Event event, Filter filter) {
        if (filter.getKindsCount() > 0 && !filter.getKindsList().contains(event.getKind())) {
            return false;
        }
        if (filter.hasField(SINCE_FIELD) && event.getCreatedAt() < filter.getSince()) {
            return false;
        }
        if (filter.hasField(UNTIL_FIELD) && event.getCreatedAt() > filter.getUntil()) {
            return false;
        }
        if (filter.getIdsCount() > 0 && !filter.getIdsList().contains(event.getId())) {
            return false;
        }
        if (filter.getAuthorsCount() > 0 && !filter.getAuthorsList().contains(event.getPubkey())) {
            return false;
        }
        if (filter.getTagsCount() > 0) {
            for (TagValue filterTag : filter.getTagsList()) {
                List<TagValue> eventTags = MoreTags.findByName(event, filterTag.getName());
                Optional<String> any = eventTags.stream()
                        .filter(it -> it.getValuesCount() > 0)
                        .map(it -> it.getValues(0))
                        .filter(it -> filterTag.getValuesList().contains(it))
                        .findAny();

                if (any.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }
}
