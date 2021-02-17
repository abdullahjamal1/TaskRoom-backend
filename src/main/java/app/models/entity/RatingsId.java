package app.models.entity;

import java.io.Serializable;
import java.util.Objects;

public class RatingsId implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6544955524076186481L;



	private Long user_id;
    private Long game_id;

    public RatingsId() {
    }

    public RatingsId(Long user_id, Long game_id) {
        this.user_id = user_id;
        this.game_id = game_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatingsId RatingsId = (RatingsId) o;
        return user_id.equals(RatingsId.user_id) &&
                game_id.equals(RatingsId.game_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, game_id);
    }
}