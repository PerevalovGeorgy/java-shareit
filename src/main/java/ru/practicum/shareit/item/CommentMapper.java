package ru.practicum.shareit.item;

import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getUser() != null ? comment.getUser().getName() : null)
                .created(comment.getCreated())
                .build();
    }

    public static List<CommentDto> toCommentDtoList(List<Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public static Comment toEntity(CommentDto commentDto, Item item, User author) {
        if (commentDto == null) {
            return null;
        }

        return Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .user(author)
                .created(commentDto.getCreated() != null ? commentDto.getCreated() : LocalDateTime.now())
                .build();
    }

    public static Comment toEntityFromCommentTextDto(CommentTextDto commentTextDto, Item item, User author, LocalDateTime time) {
        if (commentTextDto == null) {
            return null;
        }

        return Comment.builder()
                .text(commentTextDto.getText())
                .item(item)
                .user(author)
                .created(time)
                .build();
    }
}