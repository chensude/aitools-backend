@Data
@Entity
@Table(name = "verification_codes")
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    
    private String code;
    
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
} 