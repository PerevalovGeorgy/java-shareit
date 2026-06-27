package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentDto toCommentDto(Comment comment) {
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

    public List<CommentDto> toCommentDtoList(List<Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }
        return comments.stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
    }

    public Comment toEntity(CommentDto commentDto, Item item, User author) {
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
}