package com.meli.challenge.repositories;

import com.meli.challenge.domain.IP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPRepository extends JpaRepository<IP, String> {

  IP findByIp(String ip);
}