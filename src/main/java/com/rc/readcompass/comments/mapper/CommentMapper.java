package com.rc.readcompass.comments.mapper;

import com.rc.readcompass.comments.dto.CommentCreateRequest;
import com.rc.readcompass.comments.dto.CommentDto;
import com.rc.readcompass.comments.entity.Comment;
import com.rc.readcompass.review.Review;
import com.rc.readcompass.user.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "content", source = "request.content")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "review", source = "review")
  Comment toEntity(CommentCreateRequest request, User user, Review review);

  @Mapping(target = "reviewId", source = "review.id")
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userNickname", source = "user.nickname")
  CommentDto toResponse(Comment comment);

  List<CommentDto> toResponseList(List<Comment> comments);

}
