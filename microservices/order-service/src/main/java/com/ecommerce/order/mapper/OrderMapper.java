package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    
    @Mapping(target = "items", source = "items")
    OrderDTO toDTO(Order order);
    
    @Mapping(target = "order", ignore = true)
    OrderItemDTO toDTO(OrderItem orderItem);
    
    List<OrderDTO> toDTOList(List<Order> orders);
}
