package com.Vakta.Vakta_Chat_Bot.Repository;

import com.Vakta.Vakta_Chat_Bot.Model.VaktaChatBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface VaktaRepository extends JpaRepository<VaktaChatBot, UUID> {
}
